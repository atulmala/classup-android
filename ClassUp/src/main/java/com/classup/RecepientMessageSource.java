package com.classup;

/**
 * Created by atulgupta on 30/11/17.
 */

public class RecepientMessageSource {
    private String id;
    private String student;
    private String message;
    private Boolean status_extracted;
    private String status;
    private String outcome;

    public RecepientMessageSource(String id, String student, String message,
                                  Boolean status_extracted, String status, String outcome) {
        this.id = id;
        this.student = student;
        this.message = message;
        this.status_extracted = status_extracted;
        this.status = status;
        this.outcome = outcome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
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

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
