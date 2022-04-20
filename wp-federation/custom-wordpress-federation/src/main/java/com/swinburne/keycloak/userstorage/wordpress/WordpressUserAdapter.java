package com.swinburne.keycloak.userstorage.wordpress;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.federated.UserFederatedStorageProvider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordpressUserAdapter extends AbstractUserAdapterFederatedStorage {

    private final WordpressUser wpUser;
    private final String keycloakId;

    public WordpressUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, WordpressUser wpUser) {
        super(session, realm, model);
        this.wpUser = wpUser;
        this.keycloakId = StorageId.keycloakId(model, wpUser.getId());
    }

    @Override
    public UserFederatedStorageProvider getFederatedStorage() {
        // internal JPA user storage in keycloak.
        // used to store data that cannot be stored by the external federation provider
        return super.getFederatedStorage();
    }

    protected WordpressUserStorageProvider getExternalUserStorageProvider() {
        // external user storage provider
        return session.getProvider(WordpressUserStorageProvider.class, this.storageProviderModel);
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public String getUsername() {
        return wpUser.getUsername();
    }

    @Override
    public void setUsername(String username) {
        wpUser.setUsername(username);
    }

    @Override
    public String getEmail() {
        return wpUser.getEmail();
    }

    @Override
    public void setEmail(String email) {
        wpUser.setEmail(email);
    }

    @Override
    public String getFirstName() {
        return wpUser.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        wpUser.setFirstName(firstName);
    }

    @Override
    public String getLastName() {
        return wpUser.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        wpUser.setLastName(lastName);
    }

    @Override
    public Long getCreatedTimestamp() {
        return wpUser.getCreatedTimestamp();
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        //NOOP
    }

    @Override
    public boolean isEnabled() {
        return wpUser.isEnabled();
    }

    @Override
    public boolean isEmailVerified() {

        // email verified is always true for wp users
        return true;
    }

    @Override
    public void setEmailVerified(boolean verified) {
        // NOOP
    }

    @Override
    public void setEnabled(boolean enabled) {
        // super.setEnabled(enabled);
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>(wpUser.getAttributes());
        return attributes;
    }

    @Override
    public void setAttribute(String name, List<String> values) {
//        super.setAttribute(name, values);
        // NOOP
    }

    @Override
    public void setSingleAttribute(String name, String value) {
//        super.setSingleAttribute(name, value);
        // NOOP
    }

    @Override
    public List<String> getAttribute(String name) {
        return wpUser.getAttribute(name);
    }

    @Override
    public Set<RoleModel> getRoleMappings() {

        Set<RoleModel> roleMappings = new LinkedHashSet<>();

        // fetch keycloak internal role mappings
        Set<RoleModel> internalRoleMappings = super.getRoleMappings();
        roleMappings.addAll(internalRoleMappings);

        // add external roleMappings
        WordpressUserStorageProvider externalUserStorageProvider = getExternalUserStorageProvider();
        Set<RoleModel> externalRoleMappings = externalUserStorageProvider.getRoleMappings(realm, getId());
        roleMappings.addAll(externalRoleMappings);

        return roleMappings;
    }

    @Override
    public Set<RoleModel> getRealmRoleMappings() {
        // delegates to getRoleMappings
        return super.getRealmRoleMappings();
    }

    @Override
    public Set<RoleModel> getClientRoleMappings(ClientModel app) {
        // delegates to getRoleMappings
        return super.getClientRoleMappings(app);
    }
}
