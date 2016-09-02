package com.bakkenbaeck.toshi.crypto;

import com.bakkenbaeck.toshi.util.LogUtil;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static com.bakkenbaeck.toshi.crypto.util.HashUtil.sha3;

public class Wallet {

    private ECKey ecKey;

    public Wallet init() {
        this.ecKey = new ECKey();
        return this;
    }

    public Wallet initFromPrivateKey(final String privateKey) {
        final BigInteger privKey = new BigInteger(1, Hex.decode(privateKey));
        this.ecKey = ECKey.fromPrivate(privKey);
        return this;
    }

    public String sign(final String message) {
        try {
            final byte[] msgHash= sha3(message.getBytes());
            final ECKey.ECDSASignature signature = this.ecKey.sign(msgHash);
            return signature.toHex();
        } catch (final Exception e) {
            LogUtil.error(getClass(), e.toString());
        }
        return null;
    }

    public String getPrivateKey() {
        return Hex.toHexString(this.ecKey.getPrivKeyBytes());
    }

    private String getPublicKey() {
        return Hex.toHexString(this.ecKey.getPubKey());
    }

    private String getAddress() {
        return Hex.toHexString(this.ecKey.getAddress());
    }

    @Override
    public String toString() {
        return "Private: " + getPrivateKey() + "\nPublic: " + getPublicKey() + "\nAddress: " + getAddress();
    }
}
