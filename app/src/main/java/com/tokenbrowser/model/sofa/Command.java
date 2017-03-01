package com.tokenbrowser.model.sofa;

public class Command {
    private String body;
    private String value;

    public Command setBody(String body) {
        this.body = body;
        return this;
    }

    public Command setValue(String value) {
        this.value = value;
        return this;
    }
}
