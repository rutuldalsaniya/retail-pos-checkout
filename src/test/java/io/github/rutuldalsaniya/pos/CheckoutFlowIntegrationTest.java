package io.github.rutuldalsaniya.pos;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs a complete sale through the real Spring context:
 * open basket -> scan articles -> pay cash -> receipt.
 */
@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=test"
})
@AutoConfigureMockMvc
class CheckoutFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void completeCashSale() throws Exception {
        String body = mvc.perform(post("/api/baskets"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String basketId = JsonPath.read(body, "$.id");

        // 3 chocolates (3 for 2 promotion) + 1 sourdough bread (bakery -10%)
        mvc.perform(post("/api/baskets/{id}/items", basketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"4001\",\"quantity\":3}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/baskets/{id}/items", basketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"2001\",\"quantity\":1}"))
                .andExpect(status().isOk())
                // subtotal 3 x 1.29 + 3.49 = 7.36 ; discounts 0.35 + 1.29 = 1.64 ; total 5.72
                .andExpect(jsonPath("$.subtotal").value(7.36))
                .andExpect(jsonPath("$.total").value(5.72))
                .andExpect(jsonPath("$.discounts.length()").value(2));

        mvc.perform(post("/api/baskets/{id}/checkout", basketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CASH\",\"cashTendered\":10.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5.72))
                .andExpect(jsonPath("$.change").value(4.28))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"));

        mvc.perform(get("/api/baskets/{id}", basketId))
                .andExpect(jsonPath("$.status").value("PAID"));

        // a paid basket cannot be paid again
        mvc.perform(post("/api/baskets/{id}/checkout", basketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isConflict());
    }
}
