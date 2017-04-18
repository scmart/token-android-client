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


import android.os.Parcel;
import android.os.Parcelable;

import com.journeyapps.barcodescanner.BarcodeResult;

public class ScanResult implements Parcelable {

    private final String text;

    public ScanResult(final BarcodeResult result) {
        this.text = result.getText();
    }

    public String getText() {
        return this.text;
    }

    private ScanResult(final Parcel in) {
        this.text = in.readString();
    }

    public static final Creator<ScanResult> CREATOR = new Creator<ScanResult>() {
        @Override
        public ScanResult createFromParcel(final Parcel in) {
            return new ScanResult(in);
        }

        @Override
        public ScanResult[] newArray(final int size) {
            return new ScanResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(text);
    }
}
