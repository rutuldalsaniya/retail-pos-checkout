package io.github.rutuldalsaniya.pos.grpc;

import io.github.rutuldalsaniya.pos.grpc.api.PriceReply;
import io.github.rutuldalsaniya.pos.grpc.api.PriceRequest;
import io.github.rutuldalsaniya.pos.service.CatalogService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceLookupGrpcServiceTest {

    @Mock
    private StreamObserver<PriceReply> observer;

    private final PriceLookupGrpcService service = new PriceLookupGrpcService(new CatalogService());

    @Test
    void returnsPriceForKnownArticle() {
        service.getPrice(PriceRequest.newBuilder().setSku("4001").build(), observer);

        ArgumentCaptor<PriceReply> reply = ArgumentCaptor.forClass(PriceReply.class);
        verify(observer).onNext(reply.capture());
        verify(observer).onCompleted();
        assertThat(reply.getValue().getUnitPrice()).isEqualTo("1.29");
        assertThat(reply.getValue().getVatPercent()).isEqualTo("7");
    }

    @Test
    void unknownArticleReturnsNotFound() {
        service.getPrice(PriceRequest.newBuilder().setSku("9999").build(), observer);

        ArgumentCaptor<Throwable> error = ArgumentCaptor.forClass(Throwable.class);
        verify(observer).onError(error.capture());
        verify(observer, never()).onNext(any());
        assertThat(error.getValue()).isInstanceOf(StatusRuntimeException.class);
        assertThat(Status.fromThrowable(error.getValue()).getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }
}
