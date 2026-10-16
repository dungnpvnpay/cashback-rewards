package com.serenitydojo.cashback_rewards.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Basic Cashback Calculation")
class BasicCashbackCalculationIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Nested
    @DisplayName("Rule: Must calculate cashback as the merchant's configured rate multiplied by the purchase amount")
    class CalculatesCashbackAtTheMerchantRate {

        @ParameterizedTest(name = "The one where a ${2} purchase at a merchant configured at {0} earns ${3}")
        @CsvSource(textBlock = """
                5%,   0.05,  100.00, 5.00
                2.5%, 0.025, 40.00,  1.00
                10%,  0.10,  25.50,  2.55
                0%,   0.00,  100.00, 0.00
                """)
        void calculatesCashbackAtTheMerchantRate(String rateLabel,
                                                 String merchantRate,
                                                 String purchaseAmount,
                                                 String expectedCashback) throws Exception {
            String merchantName = "RateMerchant-" + merchantRate;
            registerMerchant(merchantName, true, merchantRate);

            PurchaseResponse purchase = recordPurchase("cust-rate-" + merchantRate, merchantName, purchaseAmount);

            assertThat(purchase.cashbackAmount()).isEqualByComparingTo(expectedCashback);
        }
    }

    @Nested
    @DisplayName("Rule: Must only award cashback for purchases at registered partner merchants")
    class OnlyAwardsCashbackAtPartnerMerchants {

        @Test
        @DisplayName("The one where a customer pays $80 at GreenGrocer (partner, 3%) and earns $2.40")
        void earnsCashbackAtAPartnerMerchant() throws Exception {
            registerMerchant("GreenGrocer", true, "0.03");

            PurchaseResponse purchase = recordPurchase("cust-partner", "GreenGrocer", "80.00");

            assertThat(purchase.cashbackAmount()).isEqualByComparingTo("2.40");
        }

        @Test
        @DisplayName("The one where a customer pays $80 at a registered non-partner café — the purchase is accepted, but no cashback is awarded")
        void earnsNoCashbackAtARegisteredNonPartnerMerchant() throws Exception {
            registerMerchant("Corner Café", false, "0.03");

            PurchaseResponse purchase = recordPurchase("cust-non-partner", "Corner Café", "80.00");

            assertThat(purchase.cashbackAmount()).isEqualByComparingTo("0.00");
        }
    }

    private void registerMerchant(String name, boolean partner, String cashbackRate) throws Exception {
        mockMvc.perform(post("/api/merchants")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "partner": %s,
                                  "cashbackRate": "%s"
                                }
                                """.formatted(name, partner, cashbackRate)))
                .andExpect(status().isCreated());
    }

    private PurchaseResponse recordPurchase(String customerId, String merchantName, String amount) throws Exception {
        String body = mockMvc.perform(post("/api/purchases")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "merchantName": "%s",
                                  "amount": "%s",
                                  "purchasedAt": "2026-05-01T10:00:00Z"
                                }
                                """.formatted(customerId, merchantName, amount)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(body, PurchaseResponse.class);
    }

    record PurchaseResponse(BigDecimal cashbackAmount) {
    }
}
