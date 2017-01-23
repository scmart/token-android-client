package com.bakkenbaeck.token.model.sofa;

import java.util.List;

public class Control {
    private String type;
    private String label;
    private String value;
    private String action;
    private List<Control> controls;

    public String getType() {
        return this.type;
    }

    public String getLabel() {
        return this.label;
    }

    public String getValue() {
        return this.value;
    }

    public String getAction() {
        return this.action;
    }

    public List<Control> getControls() {
        return this.controls;
    }
}
