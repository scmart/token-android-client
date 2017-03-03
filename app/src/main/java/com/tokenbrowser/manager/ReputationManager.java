package com.tokenbrowser.manager;

import com.tokenbrowser.manager.network.ReputationService;
import com.tokenbrowser.model.network.ReputationScore;

import rx.Single;
import rx.schedulers.Schedulers;

public class ReputationManager {

    public Single<ReputationScore> getReputationScore(final String ownerAddress) {
        return ReputationService
                .getApi()
                .getReputationScore(ownerAddress)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .first(response -> response.code() == 200)
                .toSingle()
                .flatMap(response -> Single.just(response.body()));
    }
}
