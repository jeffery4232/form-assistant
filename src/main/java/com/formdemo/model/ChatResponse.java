package com.formdemo.model;

public class ChatResponse {
    private String responseText;
    private String formHtml; // HTML form if needed
    private boolean hasForm;
    private String formId;
    private boolean needsClarification;
    private String intentType;

    public ChatResponse() {
    }

    public ChatResponse(String responseText, String formHtml, boolean hasForm, 
                       String formId, boolean needsClarification, String intentType) {
        this.responseText = responseText;
        this.formHtml = formHtml;
        this.hasForm = hasForm;
        this.formId = formId;
        this.needsClarification = needsClarification;
        this.intentType = intentType;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getFormHtml() {
        return formHtml;
    }

    public void setFormHtml(String formHtml) {
        this.formHtml = formHtml;
    }

    public boolean isHasForm() {
        return hasForm;
    }

    public void setHasForm(boolean hasForm) {
        this.hasForm = hasForm;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public boolean isNeedsClarification() {
        return needsClarification;
    }

    public void setNeedsClarification(boolean needsClarification) {
        this.needsClarification = needsClarification;
    }

    public String getIntentType() {
        return intentType;
    }

    public void setIntentType(String intentType) {
        this.intentType = intentType;
    }
}
