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

    
    /** 
     * @return UserFederatedStorageProvider
     */
    @Override
    public UserFederatedStorageProvider getFederatedStorage() {
        // internal JPA user storage in keycloak.
        // used to store data that cannot be stored by the external federation provider
        return super.getFederatedStorage();
    }

    
    /** 
     * @return WordpressUserStorageProvider
     */
    protected WordpressUserStorageProvider getExternalUserStorageProvider() {
        // external user storage provider
        return session.getProvider(WordpressUserStorageProvider.class, this.storageProviderModel);
    }

    
    /** 
     * @return String
     */
    @Override
    public String getId() {
        return keycloakId;
    }

    
    /** 
     * @return String
     */
    @Override
    public String getUsername() {
        return wpUser.getUsername();
    }

    
    /** 
     * @param username
     */
    @Override
    public void setUsername(String username) {
        wpUser.setUsername(username);
    }

    
    /** 
     * @return String
     */
    @Override
    public String getEmail() {
        return wpUser.getEmail();
    }

    
    /** 
     * @param email
     */
    @Override
    public void setEmail(String email) {
        wpUser.setEmail(email);
    }

    
    /** 
     * @return String
     */
    @Override
    public String getFirstName() {
        return wpUser.getFirstName();
    }

    
    /** 
     * @param firstName
     */
    @Override
    public void setFirstName(String firstName) {
        wpUser.setFirstName(firstName);
    }

    
    /** 
     * @return String
     */
    @Override
    public String getLastName() {
        return wpUser.getLastName();
    }

    
    /** 
     * @param lastName
     */
    @Override
    public void setLastName(String lastName) {
        wpUser.setLastName(lastName);
    }

    
    /** 
     * @return Long
     */
    @Override
    public Long getCreatedTimestamp() {
        return wpUser.getCreatedTimestamp();
    }

    
    /** 
     * @param timestamp
     */
    @Override
    public void setCreatedTimestamp(Long timestamp) {
        //NOOP
    }

    
    /** 
     * @return boolean
     */
    @Override
    public boolean isEnabled() {
        return wpUser.isEnabled();
    }

    
    /** 
     * @return boolean
     */
    @Override
    public boolean isEmailVerified() {

        // email verified is always true for wp users
        return true;
    }

    
    /** 
     * @param verified
     */
    @Override
    public void setEmailVerified(boolean verified) {
        // NOOP
    }

    
    /** 
     * @param enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        // super.setEnabled(enabled);
    }

    
    /** 
     * @return Map<String, List<String>>
     */
    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>(wpUser.getAttributes());
        return attributes;
    }

    
    /** 
     * @param name
     * @param values
     */
    @Override
    public void setAttribute(String name, List<String> values) {
//        super.setAttribute(name, values);
        // NOOP
    }

    
    /** 
     * @param name
     * @param value
     */
    @Override
    public void setSingleAttribute(String name, String value) {
//        super.setSingleAttribute(name, value);
        // NOOP
    }

    
    /** 
     * @param name
     * @return List<String>
     */
    @Override
    public List<String> getAttribute(String name) {
        return wpUser.getAttribute(name);
    }

    
    /** 
     * @return Set<RoleModel>
     */
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

    
    /** 
     * @return Set<RoleModel>
     */
    @Override
    public Set<RoleModel> getRealmRoleMappings() {
        // delegates to getRoleMappings
        return super.getRealmRoleMappings();
    }

    
    /** 
     * @param app
     * @return Set<RoleModel>
     */
    @Override
    public Set<RoleModel> getClientRoleMappings(ClientModel app) {
        // delegates to getRoleMappings
        return super.getClientRoleMappings(app);
    }
}
