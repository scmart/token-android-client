package com.tokenbrowser.model.sofa;

import java.io.File;

public class OutgoingAttachment {
    private File outgoingAttachment;
    private String mimeType;

    public File getOutgoingAttachment() {
        return outgoingAttachment;
    }

    public String getMimeType() {
        return mimeType;
    }

    public OutgoingAttachment setOutgoingAttachment(File outgoingAttachment) {
        this.outgoingAttachment = outgoingAttachment;
        return this;
    }

    public OutgoingAttachment setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }
}
