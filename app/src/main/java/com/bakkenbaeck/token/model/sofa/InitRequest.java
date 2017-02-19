package com.bakkenbaeck.token.model.sofa;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class InitRequest implements Parcelable {
    private List<String> values;

    protected InitRequest(Parcel in) {
        values = in.createStringArrayList();
    }

    public static final Creator<InitRequest> CREATOR = new Creator<InitRequest>() {
        @Override
        public InitRequest createFromParcel(Parcel in) {
            return new InitRequest(in);
        }

        @Override
        public InitRequest[] newArray(int size) {
            return new InitRequest[size];
        }
    };

    public List<String> getValues() {
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(values);
    }
}
