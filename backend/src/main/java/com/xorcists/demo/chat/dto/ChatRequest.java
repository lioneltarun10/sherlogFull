package com.xorcists.demo.chat.dto;

/**
 * Request DTO for chat messages
 */
public class ChatRequest {
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
