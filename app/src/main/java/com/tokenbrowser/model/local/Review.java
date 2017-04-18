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

package com.tokenbrowser.model.local;

public class Review {
    private int rating;
    private String reviewee;
    private String review;

    public Review setRating(int rating) {
        this.rating = rating;
        return this;
    }

    public Review setReviewee(String reviewee) {
        this.reviewee = reviewee;
        return this;
    }

    public Review setReview(String review) {
        this.review = review;
        return this;
    }
}
