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

package com.tokenbrowser.crypto.signal.model;


import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;

import java.util.List;

public class DecryptedSignalMessage {

    private final String body;
    private final String source;
    private Optional<List<SignalServiceAttachment>> attachments;
    private String attachmentFilePath;

    public DecryptedSignalMessage(final String source, final String body, final Optional<List<SignalServiceAttachment>> attachments) {
        this.source = source;
        this.body = body;
        this.attachments = attachments;
    }

    public DecryptedSignalMessage setAttachmentFilePath(final String attachmentFilePath) {
        this.attachmentFilePath = attachmentFilePath;
        return this;
    }

    public String getBody() {
        return body;
    }

    public String getSource() {
        return source;
    }

    public Optional<List<SignalServiceAttachment>> getAttachments() {
        return attachments;
    }

    public String getAttachmentFilePath() {
        return attachmentFilePath;
    }
}
