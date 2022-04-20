package com.swinburne.keycloak.userstorage.wordpress;

import lombok.Data;

@Data
public class WordpressRole {

    private final String id;

    private final String name;

    private final String description;
}
