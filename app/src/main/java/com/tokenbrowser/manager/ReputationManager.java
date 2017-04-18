/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
