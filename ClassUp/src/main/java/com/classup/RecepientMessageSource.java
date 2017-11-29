package com.classup;

/**
 * Created by atulgupta on 30/11/17.
 */

public class RecepientMessageSource {
    private String id;
    private String message;
    private Boolean status_extracted;
    private String status;

    public RecepientMessageSource(String id, String message,
                                  Boolean status_extracted, String status) {
        this.id = id;
        this.message = message;
        this.status_extracted = status_extracted;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getStatus_extracted() {
        return status_extracted;
    }

    public void setStatus_extracted(Boolean status_extracted) {
        this.status_extracted = status_extracted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
