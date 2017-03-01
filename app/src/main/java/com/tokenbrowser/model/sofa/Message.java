package com.tokenbrowser.model.sofa;


import java.util.List;

public class Message {

    private String body;
    private List<Control> controls;
    // Default behaviour is to how the keyboard
    private boolean showKeyboard = true;
    private List<Attachment> attachments;

    public String getBody() {
        return this.body;
    }

    public Message setBody(final String body) {
        this.body = body;
        return this;
    }

    public List<Control> getControls() {
        return this.controls;
    }

    public boolean shouldHideKeyboard() {
        return !this.showKeyboard;
    }

    public List<Attachment> getAttachments() {
        return this.attachments;
    }
}
