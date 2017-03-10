package com.tokenbrowser.crypto.signal.model;


import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;

import java.util.List;

public class DecryptedSignalMessage {

    private final String body;
    private final String source;
    private Optional<List<SignalServiceAttachment>> attachments;
    private String attachmentFilename;

    public DecryptedSignalMessage(final String source, final String body, final Optional<List<SignalServiceAttachment>> attachments) {
        this.source = source;
        this.body = body;
        this.attachments = attachments;
    }

    public DecryptedSignalMessage setAttachmentFilename(final String attachmentFilename) {
        this.attachmentFilename = attachmentFilename;
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

    public String getAttachmentFilename() {
        return attachmentFilename;
    }
}
