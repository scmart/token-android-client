package com.bakkenbaeck.token.network.ws.model;

public class PaymentRequest {
    private String type;
    private String recipient_id;
    private Payload payload;

    public PaymentRequest(String recipient_id){
        this.type = "payment_request";
        this.recipient_id = recipient_id;
        if(payload == null){
            payload = new Payload("all");
        }
    }

    private static class Payload{
        private String amount;

        public Payload(String amount){
            this.amount = amount;
        }
    }
}
