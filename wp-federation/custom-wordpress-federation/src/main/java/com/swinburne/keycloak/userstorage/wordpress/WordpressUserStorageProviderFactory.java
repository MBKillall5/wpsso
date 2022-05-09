package com.swinburne.keycloak.userstorage.wordpress;

import com.google.auto.service.AutoService;
import com.swinburne.keycloak.userstorage.wordpress.client.WpClientProvider;
import com.swinburne.keycloak.userstorage.wordpress.client.WpRestKeycloakClient;

import lombok.extern.jbosslog.JBossLog;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import javax.ws.rs.client.ClientBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class WordpressUserStorageProviderFactory implements UserStorageProviderFactory<WordpressUserStorageProvider> {

    private WordpressUserRepository repository;
    private ArrayList<WpClientProvider> restClients = new ArrayList<>();

    //@todo second node

    /** 
     * @param config
     */
    @Override
    public void init(Config.Scope config) {


    }

    
    /** 
     * @param factory
     */
    @Override
    public void postInit(KeycloakSessionFactory factory) {    
        log.infov("@mb postInit");
        repository = new WordpressUserRepository(restClients);
    }

    
    /** 
     * @param session
     * @param model
     * @return WordpressUserStorageProvider
     */
    @Override
    public WordpressUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        // here you can setup the user storage provider, initiate some connections, etc.

        log.infov("CreateProvider {0}", getId());

        restClients.clear();
        
        String wpRestEndpoint1  = model.get("wpRestEndpoint1");
        String wpAdminUsername1 = model.get("wpAdminUsernameNode1");
        String wpAdminPassword1 = model.get("wpAdminPasswordNode1");


        WpClientProvider wpClientProvider1 = new WpClientProvider(model, wpRestEndpoint1, wpAdminUsername1, wpAdminPassword1, this::createRestEasyClient);
        restClients.add(wpClientProvider1);

        return new WordpressUserStorageProvider(session, model, repository);
    }

    
    /** 
     * @return String
     */
    @Override
    public String getId() {
        return "wordpress-users";
    }

    
    /** 
     * @param session
     * @param realm
     * @param config
     */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)  throws ComponentValidationException {
        log.infov("@mb Validate configuration!");
        //throw new ComponentValidationException("@todo validate configuration");
        // String fp = config.getConfig().getFirst("path");
        // if (fp == null) throw new ComponentValidationException("user property file does not exist");
        // fp = EnvUtil.replace(fp);
        // File file = new File(fp);
        // if (!file.exists()) {
        //     throw new ComponentValidationException("user property file does not exist");
        // }
    }
    
    
    /** 
     * @return List<ProviderConfigProperty>
     */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        // this configuration is configurable in the admin-console
        return ProviderConfigurationBuilder.create()
                .property()
                .name("wpRestEndpoint1")
                .label("WP REST URL (node-1)")
                .helpText("The REST endpoint url (node-1)")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("http://192.168.56.125")
                .add()
                //admin username
                .property()
                .name("wpAdminUsernameNode1")
                .label("WP Admin Username (node-1)")
                .helpText("The WP Admin username (node-1)")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("admin")
                .add()
                //admin password
                .property()
                .name("wpAdminPasswordNode1")
                .label("WP Admin Password (node-1)")
                .helpText("The WP Admin password (node-1)")
                .type(ProviderConfigProperty.PASSWORD)
                .defaultValue("admin123")
                .add()
                .property().name("enableNode1")
                .label("Enable (node-1)")
                .helpText("Should we enable node-1")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("true")
                .add()
                .build();
    }


protected ResteasyClient createRestEasyClient(ComponentModel componentModel) {
    ResteasyClient client = new ResteasyClientBuilder() //
    .connectionPoolSize(128) // allow multiple concurrent connections.
    //.keyStore()
    //
    .build();

    return client;
    }
}