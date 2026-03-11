package com.wamessagesaver;

public class Message {
    private long id;
    private String sender;
    private String text;
    private long timestamp;
    private boolean isDeleted;
    private String mediaType; // text, image, video, audio, sticker

    public Message() {}

    public Message(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
        this.isDeleted = false;
        this.mediaType = "text";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}
