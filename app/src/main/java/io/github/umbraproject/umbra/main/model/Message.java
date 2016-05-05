package io.github.umbraproject.umbra.main.model;

/**
 * Created by matt on 5/4/16.
 */
public class Message {

    private String from;
    private String message;


    public Message(String from, String message){
        this.from = from;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
