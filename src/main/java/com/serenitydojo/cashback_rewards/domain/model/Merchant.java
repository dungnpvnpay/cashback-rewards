package com.serenitydojo.cashback_rewards.domain.model;

import java.math.BigDecimal;

public record Merchant(String name, boolean partner, BigDecimal cashbackRate) {
}
