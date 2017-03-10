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
