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

package com.tokenbrowser.model.network;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetails {

    @JsonProperty
    private String username;

    @JsonProperty
    private String payment_address;

    @JsonProperty
    private String about;

    @JsonProperty
    private String location;

    @JsonProperty
    private String name;

    public UserDetails setUsername(final String username) {
        this.username = username;
        return this;
    }

    public UserDetails setPaymentAddress(final String address) {
        this.payment_address = address;
        return this;
    }

    public UserDetails setAbout(final String about) {
        this.about = about;
        return this;
    }

    public UserDetails setLocation(final String location) {
        this.location = location;
        return this;
    }

    public UserDetails setDisplayName(final String name) {
        this.name = name;
        return this;
    }
}
