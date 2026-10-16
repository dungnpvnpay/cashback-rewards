package com.serenitydojo.cashback_rewards.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "merchants")
class MerchantEntity {

    @Id
    private String name;

    @Column(nullable = false)
    private boolean partner;

    @Column(name = "cashback_rate", nullable = false)
    private BigDecimal cashbackRate;

    protected MerchantEntity() {
    }

    MerchantEntity(String name, boolean partner, BigDecimal cashbackRate) {
        this.name = name;
        this.partner = partner;
        this.cashbackRate = cashbackRate;
    }

    String getName() {
        return name;
    }

    boolean isPartner() {
        return partner;
    }

    BigDecimal getCashbackRate() {
        return cashbackRate;
    }
}
