package it.piotrmachnik.authservice.payload.response;

import java.util.List;

public class UserInfoResponse {
  private String id;
  private String username;
  private List<String> roles;

  public UserInfoResponse(String id, String username, List<String> roles) {
    this.id = id;
    this.username = username;
    this.roles = roles;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<String> getRoles() {
    return roles;
  }
}
