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

import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.util.FileUtil;

import java.io.File;

public class OutgoingAttachment {
    private File outgoingAttachment;
    private String mimeType;

    public OutgoingAttachment(final SofaMessage sofaMessage) {
        if (sofaMessage == null || sofaMessage.getAttachmentFilePath() == null) {
            return;
        }

        this.outgoingAttachment = new File(sofaMessage.getAttachmentFilePath());
        this.mimeType = new FileUtil().getMimeTypeFromFilename(this.outgoingAttachment.getName());
    }

    public File getOutgoingAttachment() {
        return outgoingAttachment;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isValid() {
        return this.outgoingAttachment != null && this.mimeType != null;
    }
}
