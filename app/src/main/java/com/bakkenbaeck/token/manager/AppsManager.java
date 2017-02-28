package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.manager.network.DirectoryService;
import com.bakkenbaeck.token.model.network.App;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

public class AppsManager {

    private HDWallet wallet;


    public AppsManager init(final HDWallet wallet) {
        this.wallet = wallet;
        return this;
    }

    public Observable<List<App>> getRecommendedApps() {
        return DirectoryService
                .getApi()
                .getApps()
                .first((r) -> r.code() == 200)
                .flatMap((r) -> Observable.just(r.body().getApps()));
    }

    public Observable<List<App>> getFeaturedApps() {
        return DirectoryService
                .getApi()
                .getFeaturedApps()
                .subscribeOn(Schedulers.io())
                .first((r) -> r.code() == 200)
                .flatMap((r) -> Observable.just(r.body().getApps()));
    }
    public Observable<List<App>> searchApps(final String query) {
        return DirectoryService
                .getApi()
                .searchApps(query)
                .subscribeOn(Schedulers.io())
                .first((r) -> r.code() == 200)
                .flatMap((r) -> Observable.just(r.body().getApps()));
    }
}
