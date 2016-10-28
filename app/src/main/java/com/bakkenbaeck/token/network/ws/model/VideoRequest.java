package com.bakkenbaeck.token.network.ws.model;


import com.bakkenbaeck.token.network.ws.WebSocketManager;

public class VideoRequest {

    @Override
    public String toString() {
        return "{" +
                " \"type\": \"message\"," +
                " \"recipient_id\": \"" + WebSocketManager.AD_BOT_ID + "\"," +
                " \"payload\": {" +
                "   \"type\": \"new_video_request\"" +
                " }" +
                "}";
    }
}
