package com.tokenbrowser.manager;


import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.manager.network.DirectoryService;
import com.tokenbrowser.model.network.App;

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
                .first((response) -> response.code() == 200)
                .flatMap(response -> Observable.just(response.body().getApps()));
    }

    public Observable<List<App>> getFeaturedApps() {
        return DirectoryService
                .getApi()
                .getFeaturedApps()
                .subscribeOn(Schedulers.io())
                .first((response) -> response.code() == 200)
                .flatMap((response) -> Observable.just(response.body().getApps()));
    }
    public Observable<List<App>> searchApps(final String query) {
        return DirectoryService
                .getApi()
                .searchApps(query)
                .subscribeOn(Schedulers.io())
                .first((response) -> response.code() == 200)
                .flatMap((response) -> Observable.just(response.body().getApps()));
    }
}
