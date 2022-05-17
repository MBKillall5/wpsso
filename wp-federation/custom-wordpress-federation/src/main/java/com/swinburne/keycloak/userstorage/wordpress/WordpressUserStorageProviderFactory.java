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
    private static final int WP_CLIENTS_SUPPORTED = 10;

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

    @Override
    public void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        UserStorageProviderFactory.super.onUpdate(session, realm, oldModel, newModel);
        this.restClients.clear();
        this.restClients = this.getRestClients(newModel);
    }

    private ArrayList<WpClientProvider> getRestClients(ComponentModel model){
        ArrayList<WpClientProvider> result = new ArrayList<>();
        for (int i = 1; i <= WP_CLIENTS_SUPPORTED; i++) {
            String enableNode = model.get(String.format("enableNode%s", i));
            if(enableNode != null && enableNode.contentEquals("true")){
                String wpRestEndpoint  = model.get(String.format("wpRestEndpoint%s", i));
                String wpAdminUsername = model.get(String.format("wpAdminUsernameNode%s", i));
                String wpAdminPassword = model.get(String.format("wpAdminPasswordNode%s", i));

                WpClientProvider wpClientProvider = new WpClientProvider(model, wpRestEndpoint, wpAdminUsername, wpAdminPassword, this::createRestEasyClient);
                result.add(wpClientProvider);
            }
        }
        return result;
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
        this.restClients.clear();
        this.restClients = this.getRestClients(model);
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
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model)  throws ComponentValidationException {
        log.infov("@mb Validate configuration!");
        for (WpClientProvider clientProvider : this.getRestClients(model)) {
            log.infov("Reading provider from restClients: {0} ", clientProvider.toString());
            String accessToken = clientProvider.getAccessToken();
            log.infov("accessToken received from {0}: {1}", clientProvider.getAuthServerUrl(), accessToken);
            if(accessToken == null) {
                throw new ComponentValidationException(
                        String.format("Could not validate credentials with host: %s", clientProvider.getAuthServerUrl()));
            }
        }
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

        ProviderConfigurationBuilder Builder = ProviderConfigurationBuilder.create();
        // this configuration is configurable in the admin-console
        for (int i = 1; i <= WP_CLIENTS_SUPPORTED; i++) {
            Builder.property()
                    .name(String.format("wpRestEndpoint%s", i))
                    .label(String.format("WP REST URL (node-%s)", i))
                    .helpText(String.format("The REST endpoint url (node-%s)", i))
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .defaultValue("")
                    .add()
                    //admin username
                    .property()
                    .name(String.format("wpAdminUsernameNode%s", i))
                    .label(String.format("WP Admin Username (node-%s)", i))
                    .helpText(String.format("The WP Admin username (node-%s)", i))
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .defaultValue("")
                    .add()
                    //admin password
                    .property()
                    .name(String.format("wpAdminPasswordNode%s", i))
                    .label(String.format("WP Admin Password (node-%s)", i))
                    .helpText(String.format("The WP Admin password (node-%s)", i))
                    .type(ProviderConfigProperty.PASSWORD)
                    .defaultValue("")
                    .add()
                    .property().name(String.format("enableNode%s", i))
                    .label(String.format("Enable (node-%s)", i))
                    .helpText(String.format("Should we enable node-%s", i))
                    .type(ProviderConfigProperty.BOOLEAN_TYPE)
                    .defaultValue("false")
                    .add();
        }
        return Builder.build();
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