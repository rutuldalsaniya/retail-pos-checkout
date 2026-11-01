package io.github.rutuldalsaniya.pos.config;

import io.github.rutuldalsaniya.pos.service.CategoryPercentagePromotion;
import io.github.rutuldalsaniya.pos.service.MultiBuyPromotion;
import io.github.rutuldalsaniya.pos.service.Promotion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;

@Configuration
public class PosConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    Promotion bakeryDiscount() {
        return new CategoryPercentagePromotion("Bakery -10%", "BAKERY", new BigDecimal("10"));
    }

    @Bean
    Promotion chocolateThreeForTwo() {
        return new MultiBuyPromotion("Chocolate 3 for 2", "4001", 3, 2);
    }
}
