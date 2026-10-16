package com.serenitydojo.cashback_rewards.application.service;

import com.serenitydojo.cashback_rewards.application.port.in.RegisterMerchantUseCase;
import com.serenitydojo.cashback_rewards.application.port.out.MerchantRepository;
import com.serenitydojo.cashback_rewards.domain.model.Merchant;
import org.springframework.stereotype.Service;

@Service
public class RegisterMerchantService implements RegisterMerchantUseCase {

    private final MerchantRepository merchants;

    public RegisterMerchantService(MerchantRepository merchants) {
        this.merchants = merchants;
    }

    @Override
    public void register(Merchant merchant) {
        merchants.save(merchant);
    }
}
