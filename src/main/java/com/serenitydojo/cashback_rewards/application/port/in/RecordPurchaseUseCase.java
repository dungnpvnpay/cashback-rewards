package com.serenitydojo.cashback_rewards.application.port.in;

import java.math.BigDecimal;

public interface RecordPurchaseUseCase {
    BigDecimal record(String merchantName, BigDecimal amount);
}
