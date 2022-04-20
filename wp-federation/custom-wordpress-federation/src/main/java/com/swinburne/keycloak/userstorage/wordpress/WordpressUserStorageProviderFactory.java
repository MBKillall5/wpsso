package com.swinburne.keycloak.userstorage.wordpress;

import com.google.auto.service.AutoService;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class WordpressUserStorageProviderFactory implements UserStorageProviderFactory<WordpressUserStorageProvider> {

    WordpressUserRepository repository;

    
    /** 
     * @param config
     */
    @Override
    public void init(Config.Scope config) {

        // String someProperty = config.get("wpRestEndpoint");
        // log.infov("Configured {0} with someProperty: {1}", this, someProperty);
    }

    
    /** 
     * @param factory
     */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        repository = new WordpressUserRepository();
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
                .name("wpRestEndpoint")
                .label("WP REST URL (node-1)")
                .helpText("The REST endpoint url (node-1)")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("http://192.168.56.152/wp/")
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
                .defaultValue("admin")
                .add()
                .property().name("enableNode1")
                .label("Enable (node-1)")
                .helpText("Should we enable node-1")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("true")
                .add()
                .build();
    }
}
