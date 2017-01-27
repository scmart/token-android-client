package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;

import java.util.concurrent.Callable;

import rx.Single;

public class TokenManager {

    private BalanceManager balanceManager;
    private HDWallet wallet;
    private ChatMessageManager chatMessageManager;
    private TransactionManager transactionManager;
    private UserManager userManager;

    public TokenManager() {
        this.balanceManager = new BalanceManager();
        this.userManager = new UserManager();
        this.chatMessageManager = new ChatMessageManager();
        this.transactionManager = new TransactionManager();
    }

    public Single<TokenManager> init() {
        return Single.fromCallable(new Callable<TokenManager>() {
            @Override
            public TokenManager call() throws Exception {
                TokenManager.this.wallet = new HDWallet().init();
                TokenManager.this.balanceManager.init(TokenManager.this.wallet);
                TokenManager.this.chatMessageManager.init(TokenManager.this.wallet);
                TokenManager.this.transactionManager.init(TokenManager.this.wallet);
                TokenManager.this.userManager.init(TokenManager.this.wallet);
                return TokenManager.this;
            }
        });
    }

    public final ChatMessageManager getChatMessageManager() {
        return this.chatMessageManager;
    }

    public final TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    public final UserManager getUserManager() {
        return this.userManager;
    }

    public final BalanceManager getBalanceManager() {
        return this.balanceManager;
    }

    public Single<HDWallet> getWallet() {
        return Single.fromCallable(new Callable<HDWallet>() {
            @Override
            public HDWallet call() throws Exception {
                while (wallet == null) {
                    Thread.sleep(200);
                }
                return wallet;
            }
        });
    }

}
