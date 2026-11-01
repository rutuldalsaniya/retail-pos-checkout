package io.github.rutuldalsaniya.pos.grpc;

import io.github.rutuldalsaniya.pos.domain.Product;
import io.github.rutuldalsaniya.pos.grpc.api.PriceLookupServiceGrpc;
import io.github.rutuldalsaniya.pos.grpc.api.PriceReply;
import io.github.rutuldalsaniya.pos.grpc.api.PriceRequest;
import io.github.rutuldalsaniya.pos.service.CatalogService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

@GrpcService
public class PriceLookupGrpcService extends PriceLookupServiceGrpc.PriceLookupServiceImplBase {

    private final CatalogService catalog;

    public PriceLookupGrpcService(CatalogService catalog) {
        this.catalog = catalog;
    }

    @Override
    public void getPrice(PriceRequest request, StreamObserver<PriceReply> responseObserver) {
        Optional<Product> product = catalog.find(request.getSku());
        if (product.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Unknown article " + request.getSku())
                    .asRuntimeException());
            return;
        }
        Product p = product.get();
        responseObserver.onNext(PriceReply.newBuilder()
                .setSku(p.sku())
                .setName(p.name())
                .setUnitPrice(p.unitPrice().toPlainString())
                .setVatPercent(p.vatRate().percent().toPlainString())
                .build());
        responseObserver.onCompleted();
    }
}
