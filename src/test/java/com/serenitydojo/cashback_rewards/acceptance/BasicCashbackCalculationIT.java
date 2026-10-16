package com.serenitydojo.cashback_rewards.acceptance;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
            String customerId = "cust-merchant-rate-" + merchantRate;
            String merchantName = "RateMerchant-" + merchantRate;

            registerPartnerMerchant(merchantName, merchantRate);

            recordPurchase(customerId, merchantName, purchaseAmount, "5999");

            List<CashbackRecord> records = cashbackFor(customerId);

            assertThat(records).hasSize(1);
            assertThat(records.getFirst().cashbackAmount()).isEqualByComparingTo(expectedCashback);
        }
    }

    @Nested
    @DisplayName("Rule: Must only award cashback for purchases at partner merchants")
    class OnlyAwardsCashbackAtPartnerMerchants {

        @Test
        @DisplayName("The one where a customer shops at a partner merchant and earns cashback")
        void earnsCashbackAtPartnerMerchant() throws Exception {
            registerCategory("5411", "Groceries", "0.02");
            registerMerchant("GreenGrocer", true);

            recordPurchase("cust-001", "GreenGrocer", "80.00", "5411");

            List<CashbackRecord> records = cashbackFor("cust-001");

            assertThat(records).hasSize(1);
            assertThat(records.getFirst().merchantName()).isEqualTo("GreenGrocer");
        }

        @Test
        @DisplayName("The one where a customer shops at a non-partner merchant — no cashback record is created")
        void doesNotEarnCashbackAtNonPartnerMerchant() throws Exception {
            registerCategory("5411", "Groceries", "0.02");
            registerMerchant("The Corner Café", false);

            recordPurchase("cust-002", "The Corner Café", "80.00", "5411");

            List<CashbackRecord> records = cashbackFor("cust-002");

            assertThat(records).isEmpty();
        }
    }

    private void registerCategory(String mcc, String name, String cashbackRate) throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "mcc": "%s",
                                  "name": "%s",
                                  "cashbackRate": "%s"
                                }
                                """.formatted(mcc, name, cashbackRate)))
                .andExpect(status().isCreated());
    }

    private void registerMerchant(String name, boolean partner) throws Exception {
        mockMvc.perform(post("/api/merchants")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "partner": %s
                                }
                                """.formatted(name, partner)))
                .andExpect(status().isCreated());
    }

    private void registerPartnerMerchant(String name, String cashbackRate) throws Exception {
        mockMvc.perform(post("/api/merchants")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "partner": true,
                                  "cashbackRate": "%s"
                                }
                                """.formatted(name, cashbackRate)))
                .andExpect(status().isCreated());
    }

    private void recordPurchase(String customerId, String merchantName, String amount, String mcc) throws Exception {
        mockMvc.perform(post("/api/purchases")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "merchantName": "%s",
                                  "amount": "%s",
                                  "mcc": "%s",
                                  "purchasedAt": "2026-05-01T10:00:00Z"
                                }
                                """.formatted(customerId, merchantName, amount, mcc)))
                .andExpect(status().is2xxSuccessful());
    }

    private List<CashbackRecord> cashbackFor(String customerId) throws Exception {
        String body = mockMvc.perform(get("/api/customers/{customerId}/cashback", customerId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(body, new TypeReference<>() {
        });
    }

    record CashbackRecord(String merchantName, String productCategory, BigDecimal cashbackAmount) {
    }
}