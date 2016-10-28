package com.bakkenbaeck.token.network.ws.model;


import java.util.List;

public class Message {

    private MessageInternals payload;
    private String type;

    private static class Internals {
        private MessageInternals message;
    }

    private static class MessageInternals {
        private String text;
        private List<Detail> details;
        private List<Action> actions;
        private String type;
    }

    @Override
    public String toString() {
        return this.payload.text;
    }

    public List<Detail> getDetails(){
        return this.payload.details;
    }

    public String getType(){
        return this.payload.type;
    }

    public void setType(String type){
        this.payload.type = type;
    }

    public List<Action> getActions(){
        return payload.actions;
    }

    public boolean shouldShowVideo(){
        return     getActions() != null
                && getActions().size() == 1
                && getActions().get(0).getAction().equals("show_video");
    }
}
