package com.ims.tool;

import java.util.Optional;

public class Response {

  private int httpStatus;

  private String responseBody;

  private Optional<String> sessionId = Optional.empty();

  public int getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(int httpStatus) {
    this.httpStatus = httpStatus;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  public Optional<String> getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = Optional.ofNullable(sessionId);
  }
}
