package com.bakkenbaeck.toshi.crypto;


import com.bakkenbaeck.toshi.util.LogUtil;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

public class Wallet {

    private String privateKey;
    private String publicKey;
    private String address;

    private static final ECDomainParameters CURVE;

    static {
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }


    public Wallet init() {
        final KeyPair kp = generateKeyPair();
        this.privateKey =  Hex.toHexString(((BCECPrivateKey) kp.getPrivate()).getD().toByteArray());
        this.publicKey = Hex.toHexString(((BCECPublicKey) kp.getPublic()).getQ().getEncoded(false));
        generateAddress();
        return this;
    }

    private KeyPair generateKeyPair() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDsA", "SC");
            final ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (final NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            LogUtil.e(getClass(), e.toString());
        }
        return null;
    }

    public Wallet initFromPrivateKey(final String privateKey) {
        this.privateKey = privateKey;

        final BigInteger privKey = new BigInteger(1, Hex.decode(privateKey));
        final ECPoint publicKey = CURVE.getG().multiply(privKey);
        this.publicKey = Hex.toHexString(publicKey.getEncoded(false));
        generateAddress();
        return this;
    }

    private void generateAddress() {
        final byte[] addressBytes = computeAddress(Hex.decode(this.publicKey));
        this.address = Hex.toHexString(addressBytes);
    }

    private byte[] computeAddress(byte[] pubBytes) {
        return HashUtil.sha3omit12(Arrays.copyOfRange(pubBytes, 1, pubBytes.length));
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    @Override
    public String toString() {
        LogUtil.print(getClass(), "Private: " + this.privateKey);
        LogUtil.print(getClass(), "Public: " + this.publicKey);
        return "Private: " + this.privateKey + "\nPublic: " + this.publicKey + "\nAddress: " + this.address;
    }
}
