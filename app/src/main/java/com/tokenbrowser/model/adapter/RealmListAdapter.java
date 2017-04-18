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
