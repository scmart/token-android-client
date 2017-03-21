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
