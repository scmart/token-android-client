package com.bakkenbaeck.token.model;


import com.bakkenbaeck.token.util.EthUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

// Represents both confirmed and unconfirmed balances in wei for the device.
// It may be out of sync with reality.
public class LocalBalance {

    // Both balances are stored in wei
    private BigInteger confirmedBalance = BigInteger.ZERO;
    private BigInteger unconfirmedBalance = BigInteger.ZERO;
    private BigInteger transferFee = new BigInteger("420000000000000");

    public BigInteger getConfirmedBalance() {
        return confirmedBalance;
    }

    public void setConfirmedBalance(final BigInteger confirmedBalance) {
        this.confirmedBalance = confirmedBalance;
    }

    public BigDecimal getConfirmedBalanceAsEth() {
        return EthUtil.weiToEth(this.confirmedBalance);
    }

    public BigDecimal getConfirmedBalanceAsEthMinusTransferFee() {
        if (this.confirmedBalance == null || this.confirmedBalance.subtract(transferFee).compareTo(BigInteger.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return EthUtil.weiToEth(this.confirmedBalance.subtract(transferFee));
    }

    public BigDecimal getUnconfirmedBalanceAsEthMinusTransferFee() {
        if (this.unconfirmedBalance == null || this.unconfirmedBalance.subtract(transferFee).compareTo(BigInteger.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return EthUtil.weiToEth(this.unconfirmedBalance.subtract(transferFee));
    }

    public String confirmedBalanceString() {
        if (this.confirmedBalance == null || this.confirmedBalance.equals(BigInteger.ZERO)) {
            return "0";
        }
        return EthUtil.weiToEthString(this.confirmedBalance);
    }

    public String confirmedBalanceStringMinusTransferFee() {
        if (this.confirmedBalance == null || this.confirmedBalance.subtract(transferFee).compareTo(BigInteger.ZERO) <= 0) {
            return "0";
        }
        return EthUtil.weiToEthString(this.confirmedBalance.subtract(transferFee));
    }

    public String unconfirmedBalanceStringMinusTransferFee() {
        if (this.unconfirmedBalance == null || this.unconfirmedBalance.subtract(transferFee).compareTo(BigInteger.ZERO) <= 0) {
            return "0";
        }
        return EthUtil.weiToEthString(this.unconfirmedBalance.subtract(transferFee));
    }

    public void setUnconfirmedBalance(final BigInteger unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public BigInteger getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public BigDecimal getUnconfirmedBalanceAsEth() {
        return EthUtil.weiToEth(this.unconfirmedBalance);
    }

    public String unconfirmedBalanceString() {
        if (this.unconfirmedBalance == null || this.unconfirmedBalance.equals(BigInteger.ZERO)) {
            return "0";
        }
        return EthUtil.weiToEthString(this.unconfirmedBalance);
    }

}
