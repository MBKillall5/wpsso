package com.swinburne.keycloak.userstorage.wordpress;

import com.google.auto.service.AutoService;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class WordpressUserStorageProviderFactory implements UserStorageProviderFactory<WordpressUserStorageProvider> {

    WordpressUserRepository repository;

    @Override
    public void init(Config.Scope config) {

        // this configuration is pulled from the SPI configuration of this provider in the standalone[-ha] / domain.xml
        // see setup.cli

        String someProperty = config.get("someProperty");
        log.infov("Configured {0} with someProperty: {1}", this, someProperty);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        repository = new WordpressUserRepository();
    }

    @Override
    public WordpressUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        // here you can setup the user storage provider, initiate some connections, etc.

        log.infov("CreateProvider {0}", getId());

        return new WordpressUserStorageProvider(session, model, repository);
    }

    @Override
    public String getId() {
        return "wordpress-users";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        // this configuration is configurable in the admin-console
        return ProviderConfigurationBuilder.create()
                .property()
                .name("myParam")
                .label("Some bogus param bla")
                .helpText("Some Description")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("some value 2")
                .add()
                // more properties
                // .property()
                // .add()
                .build();
    }
}
