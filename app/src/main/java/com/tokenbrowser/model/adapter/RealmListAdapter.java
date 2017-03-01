package com.tokenbrowser.model.adapter;

import com.tokenbrowser.model.local.RealmString;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class RealmListAdapter {
    @ToJson
    public List<String> toJson(RealmList<RealmString> intList) {
        final List<String> outList = new ArrayList<>();

        for (final RealmString s : intList) {
            outList.add(s.getValue());
        }

        return outList;
    }

    @FromJson
    public RealmList<RealmString> fromJson(final List<String> inList) {
        final RealmList<RealmString> outList = new RealmList<>();

        for (final String s : inList) {
            outList.add(new RealmString(s));
        }

        return outList;
    }
}
