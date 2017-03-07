package com.tokenbrowser.manager;

import com.tokenbrowser.manager.network.ReputationService;
import com.tokenbrowser.model.local.Review;
import com.tokenbrowser.model.network.ReputationScore;

import retrofit2.Response;
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

    public Single<Response<Void>> submitReview(final Review review, final long timeStamp) {
        return ReputationService
                .getApi()
                .submitReview(review, timeStamp)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .first(response -> response.code() == 204)
                .toSingle();
    }
}
