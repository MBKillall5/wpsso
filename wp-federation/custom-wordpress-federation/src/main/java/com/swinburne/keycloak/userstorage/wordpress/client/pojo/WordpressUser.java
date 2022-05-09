package com.swinburne.keycloak.userstorage.wordpress.client.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import  com.swinburne.keycloak.userstorage.wordpress.WordpressRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
//@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordpressUser {

    @JsonIgnore
    private boolean enabled = true;
    @JsonIgnore
    private long createdTimestamp = 0;
    @JsonIgnore
    private String password = "na";
    @JsonIgnore
    private Map<String, List<String>> attributes=Map.of("attribute1", List.of("value1_1"));

    private String id;
    private String username;
    private String email; /**/
    private String first_name;
    private String last_name;
    private List<String> roles=Collections.emptyList();
    private String registered_date;
    private HashMap<String, Boolean> capabilities;

    /** 
     * @param name
     * @return List<String>
     */
    public List<String> getAttribute(String name) {

        return attributes.getOrDefault(name, Collections.emptyList());
    }

    public Map<String, List<String>> getAttributes() {
        HashMap <String, List<String>> ret_attributes = new HashMap<>();
        for (String key: capabilities.keySet()) {
            ret_attributes.put(key, List.of(capabilities.get(key).toString()));
        }
        return ret_attributes;
    }

    /** 
     * @param name
     * @return List<String>
     */
    public List<String> getRoles() {
        return roles;
    }

    public Set<WordpressRole> getRolesAsWordpressRole() {
        Set<WordpressRole> roles = new HashSet<>();

        for (String role: this.getRoles()) {
            roles.add(new WordpressRole("wp_"+role, "wp_"+role,"This is the Wordpress role:"+role));
        }
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
