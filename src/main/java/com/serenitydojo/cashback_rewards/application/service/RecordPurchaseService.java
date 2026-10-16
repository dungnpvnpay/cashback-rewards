package com.serenitydojo.cashback_rewards.application.service;

import com.serenitydojo.cashback_rewards.application.port.in.RecordPurchaseUseCase;
import com.serenitydojo.cashback_rewards.application.port.out.MerchantRepository;
import com.serenitydojo.cashback_rewards.domain.model.Merchant;
import com.serenitydojo.cashback_rewards.domain.service.CashbackCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RecordPurchaseService implements RecordPurchaseUseCase {

    private final MerchantRepository merchants;

    public RecordPurchaseService(MerchantRepository merchants) {
        this.merchants = merchants;
    }

    @Override
    public BigDecimal record(String merchantName, BigDecimal amount) {
        Merchant merchant = merchants.findByName(merchantName)
                .orElseThrow(() -> new IllegalStateException("Merchant not found: " + merchantName));
        return CashbackCalculator.calculate(amount, merchant.cashbackRate());
    }
}
