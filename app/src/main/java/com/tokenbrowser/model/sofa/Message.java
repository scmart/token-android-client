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


import java.util.List;

public class Message {

    private String body;
    private List<Control> controls;
    // Default behaviour is to how the keyboard
    private boolean showKeyboard = true;
    private String attachmentFilename;

    public String getBody() {
        return this.body;
    }

    public Message setBody(final String body) {
        this.body = body;
        return this;
    }

    public Message setAttachmentFilename(final String filename) {
        this.attachmentFilename = filename;
        return this;
    }

    public List<Control> getControls() {
        return this.controls;
    }

    public boolean shouldHideKeyboard() {
        return !this.showKeyboard;
    }

    public String getAttachmentFilename() {
        return attachmentFilename;
    }
}
