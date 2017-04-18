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

import io.realm.RealmList;
import io.realm.RealmObject;

public class CustomAppInformation extends RealmObject {
    private String paymentAddress;
    private String webApp;
    private String displayName;
    private String protocol;
    private String avatarUrl;
    private RealmList<RealmString> languages;
    private RealmList<RealmString> interfaces;
}
