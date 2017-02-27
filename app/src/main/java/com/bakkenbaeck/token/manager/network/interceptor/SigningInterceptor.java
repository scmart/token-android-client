package com.bakkenbaeck.token.manager.network.interceptor;


import android.util.Base64;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.crypto.util.HashUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

public class SigningInterceptor implements Interceptor {

    private HDWallet wallet;

    private final String TIMESTAMP_QUERY_PARAMETER = "timestamp";
    private final String ADDRESS_HEADER = "Token-ID-Address";
    private final String SIGNATURE_HEADER = "Token-Signature";
    private final String TIMESTAMP_HEADER = "Token-Timestamp";

    public SigningInterceptor() {
        BaseApplication
                .get()
                .getTokenManager()
                .getWallet()
                .subscribe(new SingleSuccessSubscriber<HDWallet>() {
                    @Override
                    public void onSuccess(final HDWallet wallet) {
                        SigningInterceptor.this.wallet = wallet;
                    }
                });
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Request original = chain.request();
        final String timestamp = original.url().queryParameter(TIMESTAMP_QUERY_PARAMETER);
        if (this.wallet == null || timestamp == null) {
            // Only signing outgoing requests that have a timestamp argument
            return chain.proceed(original);
        }

        final Buffer buffer = new Buffer();
        final String method = original.method();
        final String path = original.url().encodedPath();
        String encodedBody = "";
        if (original.body() != null) {
            original.body().writeTo(buffer);
            final byte[] body = buffer.readByteArray();
            final byte[] hashedBody = HashUtil.sha3(body);
            encodedBody = Base64.encodeToString(hashedBody, Base64.NO_WRAP);
        }

        final String forSigning = method + "\n" + path + "\n" + timestamp + "\n" + encodedBody;
        final String signature = this.wallet.signIdentity(forSigning);

        final HttpUrl url = chain.request().url()
                .newBuilder()
                .removeAllQueryParameters(TIMESTAMP_QUERY_PARAMETER)
                .build();

        final Request request = original.newBuilder()
                .removeHeader(TIMESTAMP_QUERY_PARAMETER)
                .method(original.method(), original.body())
                .addHeader(TIMESTAMP_HEADER, timestamp)
                .addHeader(SIGNATURE_HEADER, signature)
                .addHeader(ADDRESS_HEADER, this.wallet.getOwnerAddress())
                .url(url)
                .build();

        return chain.proceed(request);
    }
}
