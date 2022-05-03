package com.swinburne.keycloak.userstorage.wordpress.client.pojo;

public class AccessTokenRequest {
    private String username;
    private String password;
    public AccessTokenRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
        
}
