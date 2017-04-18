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

package com.tokenbrowser.model.sofa;

import com.tokenbrowser.util.LocaleUtil;

public class Init {
    private String paymentAddress;
    private String language;

    public Init construct(final InitRequest initRequest, final String paymentAddress) {
        for (String value : initRequest.getValues()) {
            switch (value) {
                case SofaType.LANGUAGE: {
                    this.language = LocaleUtil.getLocale().getLanguage();
                    break;
                }
                case SofaType.PAYMENT_ADDRESS: {
                    this.paymentAddress = paymentAddress;
                }
            }
        }

        return this;
    }
}
