package com.swinburne.keycloak.userstorage.wordpress.client.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordpressUser {

    @JsonIgnore
    private boolean enabled;
    @JsonIgnore
    private long createdTimestamp;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private Map<String, List<String>> attributes;

    private String id;
    private String username;
    private String email; /**/
    private String first_name;
    private String last_name;
    private ArrayList<String> roles;
    
    /** 
     * @param name
     * @return List<String>
     */
    public List<String> getAttribute(String name) {
        return attributes.getOrDefault(name, Collections.emptyList());
    }

    /** 
     * @param name
     * @return List<String>
     */
    public List<String> getRoles() {
        return roles;
    }


    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String name) {
        this.first_name = name;
    }

    public String getLastName() {
        return last_name;
    }
    
    public void setLastName(String name) {
        this.last_name = name;
    }

}
