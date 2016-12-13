package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.model.LocalBalance;

public class BalanceManager {

    private LocalBalance localBalance;

    public BalanceManager init() {
        this.localBalance = new LocalBalance();
        return this;
    }

    @Override
    public String toString() {
        return this.localBalance.unconfirmedBalanceString();
    }
}