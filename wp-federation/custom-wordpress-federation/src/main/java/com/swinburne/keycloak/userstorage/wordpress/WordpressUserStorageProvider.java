package com.swinburne.keycloak.userstorage.wordpress;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.policy.PasswordPolicyManagerProvider;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.federated.UserAttributeFederatedStorage;
import org.keycloak.storage.federated.UserRoleMappingsFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.swinburne.keycloak.userstorage.wordpress.client.WpRestKeycloakClient;
import com.swinburne.keycloak.userstorage.wordpress.client.WpClientProvider;


/**
 * Note that this custom UserStorageProvider does NOT support caching!
 * Disable caching for the User Federation!
 */
@JBossLog
public class WordpressUserStorageProvider implements
        UserStorageProvider
        , UserLookupProvider
        , UserQueryProvider

        , UserRegistrationProvider

        , UserAttributeFederatedStorage

        , CredentialInputUpdater
        , CredentialInputValidator

        , UserRoleMappingsFederatedStorage

//        ,OnUserCache
{

    private final KeycloakSession session;
    private final ComponentModel storageComponentModel;
    private final WordpressUserRepository repository;
    

    public WordpressUserStorageProvider(KeycloakSession session,
            ComponentModel storageComponentModel,
            WordpressUserRepository repository) {

        this.session = session;
        this.storageComponentModel = storageComponentModel;
        this.repository = repository;
    }

    
    /** 
     * @param credentialType
     * @return boolean
     */
    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    
    /** 
     * @param realm
     * @param user
     * @param credentialType
     * @return boolean
     */
    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    
    /** 
     * @param realm
     * @param user
     * @param input
     * @return boolean
     */
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {

        log.infov("isValid user credential: userId={0}", user.getId());

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        UserCredentialModel cred = (UserCredentialModel) input;
        return repository.validateCredentials(user.getUsername(), cred.getValue());

        //@mb here we can store the new credential
    }

    
    /** 
     * @param realm
     * @param user
     * @param input
     * @return boolean
     */
    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {

        log.infov("updating credential: realm={0} user={1}", realm.getId(), user.getUsername());

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        UserCredentialModel cred = (UserCredentialModel) input;
        // session.getProvider(PasswordPolicyManagerProvider.class).validate(realm, user, cred.getValue());
        // @mb or session.userCredentialManager().updateCredential(realm, localUser, UserCredentialModel.password(credentialInput.getChallengeResponse(), false));
        return repository.updateCredentials(user.getUsername(), cred.getValue());
    }

    
    /** 
     * @param realm
     * @param user
     * @param credentialType
     */
    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        log.infov("disable credential type: realm={0} user={1} credentialType={2}", realm.getId(), user.getUsername(), credentialType);
    }

    
    /** 
     * @param realm
     * @param user
     * @return Set<String>
     */
    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        return Collections.emptySet();
    }

    
    /** 
     * @param realm
     */
    @Override
    public void preRemove(RealmModel realm) {

        log.infov("pre-remove realm");
    }

    
    /** 
     * @param realm
     * @param group
     */
    @Override
    public void preRemove(RealmModel realm, GroupModel group) {

        log.infov("pre-remove group");
    }

    
    /** 
     * @param realm
     * @param role
     */
    @Override
    public void preRemove(RealmModel realm, RoleModel role) {

        log.infov("pre-remove role");
    }

    @Override
    public void close() {
        log.infov("closing");
    }

    
    /** 
     * @param id
     * @param realm
     * @return UserModel
     */
    @Override
    public UserModel getUserById(String id, RealmModel realm) {

        log.infov("lookup user by id: realm={0} userId={1}", realm.getId(), id);

        String externalId = StorageId.externalId(id);
        return createAdapter(realm, repository.findUserById(externalId));
    }


/** 
 * @return UserModel
 */
//    @Override
//    public UserModel getUserByUsername(RealmModel realm, String username) {
//        return UserLookupProvider.super.getUserByUsername(realm, username);
//    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {

        log.infov("lookup user by username: realm={0} username={1}", realm.getId(), username);
        return createAdapter(realm, repository.findUserByUsernameOrEmail(username));
    }

    
    /** 
     * @param realm
     * @param wpUser
     * @return UserModel
     */
    protected UserModel createAdapter(RealmModel realm, WordpressUser wpUser) {

        if (wpUser == null) {
            return null;
        }

        WordpressUserAdapter wpUserAdapter = new WordpressUserAdapter(session, realm, storageComponentModel, wpUser);
        return wpUserAdapter;
    }

    
    /** 
     * @param email
     * @param realm
     * @return UserModel
     */
    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {

        log.infov("lookup user by username: realm={0} email={1}", realm.getId(), email);

        return getUserByUsername(email, realm);
    }

    
    /** 
     * @param realm
     * @return int
     */
    @Override
    public int getUsersCount(RealmModel realm) {
        return repository.getUsersCount();
    }

    
    /** 
     * @param realm
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> getUsers(RealmModel realm) {

        log.infov("list users: realm={0}", realm.getId());

        return repository.getAllUsers().stream()
                .map(wpUser -> new WordpressUserAdapter(session, realm, storageComponentModel, wpUser))
                .collect(Collectors.toList());
    }

    
    /** 
     * @param realm
     * @param firstResult
     * @param maxResults
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {

        log.infov("list users: realm={0} firstResult={1} maxResults={2}", realm.getId(), firstResult, maxResults);

        return getUsers(realm);
    }

    
    /** 
     * @param search
     * @param realm
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {

        log.infov("search for users: realm={0} search={1}", realm.getId(), search);

        return searchForUser(search, realm, 0, -1);
    }

    
    /** 
     * @param search
     * @param realm
     * @param firstResult
     * @param maxResults
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {

        log.infov("search for users: realm={0} search={1} firstResult={2} maxResults={3}", realm.getId(), search, firstResult, maxResults);

        if (search.contains(":")) {
            String attributeName = search.substring(0, search.indexOf(":"));
            String attributeValue = search.substring(search.indexOf(":") + 1);

            return repository.findUsersByAttribute(attributeName, attributeValue, firstResult, maxResults).stream()
                    .map(id -> new WordpressUserAdapter(session, realm, storageComponentModel, repository.findUserById(id)))
                    .collect(Collectors.toList());
        }

        return repository.findUsers(search, firstResult, maxResults).stream()
                .map(wpUser -> new WordpressUserAdapter(session, realm, storageComponentModel, wpUser))
                .collect(Collectors.toList());
    }

    
    /** 
     * @param params
     * @param realm
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {

        log.infov("search for users with params: realm={0} params={1}", realm.getId(), params);

        return searchForUser(params, realm, 0, -1);
    }

    
    /** 
     * @param params
     * @param realm
     * @param firstResult
     * @param maxResults
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {

        log.infov("search for users with params: realm={0} params={1} firstResult={2} maxResults={3}", realm.getId(), params, firstResult, maxResults);

        // use params from org.keycloak.models.UserModel

        return searchForUser("", realm);
    }

    
    /** 
     * @param realm
     * @param group
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {

        log.infov("search for group members: realm={0} groupId={1} firstResult={2} maxResults={3}", realm.getId(), group.getId());

        return getGroupMembers(realm, group, 0, -1);
    }

    
    /** 
     * @param realm
     * @param group
     * @param firstResult
     * @param maxResults
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {

        log.infov("search for group members with params: realm={0} groupId={1} firstResult={2} maxResults={3}", realm.getId(), group.getId(), firstResult, maxResults);

        return Collections.emptyList();
    }

    
    /** 
     * @param attrName
     * @param attrValue
     * @param realm
     * @return List<UserModel>
     */
    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {

        log.infov("search for user by attribute: realm={0} attrName={1} attrValue={2}", realm.getId(), attrName, attrValue);

        return repository.findUsersByAttribute(attrName, attrValue, 0, -1).stream()
                .map(id -> new WordpressUserAdapter(session, realm, storageComponentModel, repository.findUserById(id)))
                .collect(Collectors.toList());
    }

    
    /** 
     * @param realm
     * @param userId
     * @param role
     */
    /* UserRoleMappingsFederatedStorage start */
    @Override
    public void grantRole(RealmModel realm, String userId, RoleModel role) {
        log.infov("grant role mapping: realm={0} userId={1} role={2}", realm.getId(), userId, role.getName());
    }

    
    /** 
     * @param realm
     * @param userId
     * @return Set<RoleModel>
     */
    @Override
    public Set<RoleModel> getRoleMappings(RealmModel realm, String userId) {
        log.infov("get role mappings: realm={0} userId={1}", realm.getId(), userId);

        String externalUserId = StorageId.externalId(userId);

        Set<WordpressRole> roles = repository.getGlobalRolesByUserId(externalUserId);
        Set<RoleModel> externalRoles = roles.stream()
                .map(role -> new WordpressRoleModel(role.getId(), role.getName(), role.getDescription(), false, realm))
                .collect(Collectors.toSet());

        for (ClientModel client : realm.getClients()) {

            String clientId = client.getClientId();
            // potentially filter for wp clients...

            Set<WordpressRole> clientRolesByUserId = repository.getClientRolesByUserId(clientId, externalUserId);
            if (clientRolesByUserId != null) {
                Set<RoleModel> externalClientRoles = clientRolesByUserId.stream()
                        .map(role -> new WordpressRoleModel(role.getId(), role.getName(), role.getDescription(), false, client))
                        .collect(Collectors.toSet());
                externalRoles.addAll(externalClientRoles);
            }
        }

        return externalRoles;
    }

    
    /** 
     * @param realm
     * @param userId
     * @param role
     */
    @Override
    public void deleteRoleMapping(RealmModel realm, String userId, RoleModel role) {
        log.infov("delete role mapping: realm={0} userId={1} role={2}", realm.getId(), userId, role.getName());
    }

    
    /** 
     * @param realm
     * @param userId
     * @param name
     * @param value
     */
    /* UserRoleMappingsFederatedStorage end */

    @Override
    public void setSingleAttribute(RealmModel realm, String userId, String name, String value) {
        log.infov("set single attribute: realm={0} userId={1} name={2} value={3}", realm.getId(), userId, name, value);
    }

    
    /** 
     * @param realm
     * @param userId
     * @param name
     * @param values
     */
    @Override
    public void setAttribute(RealmModel realm, String userId, String name, List<String> values) {
        log.infov("set attribute: realm={0} userId={1} name={2} value={3}", realm.getId(), userId, name, values);
    }

    
    /** 
     * @param realm
     * @param userId
     * @param name
     */
    @Override
    public void removeAttribute(RealmModel realm, String userId, String name) {
        log.infov("remove attribute: realm={0} userId={1} name={2}", realm.getId(), userId, name);
    }

    
    /** 
     * @param realm
     * @param userId
     * @return MultivaluedHashMap<String, String>
     */
    @Override
    public MultivaluedHashMap<String, String> getAttributes(RealmModel realm, String userId) {

        log.infov("get attributes: realm={0} userId={1}", realm.getId(), userId);

        String externalId = StorageId.externalId(userId);
        WordpressUser wpUser = repository.findUserById(externalId);

        return new MultivaluedHashMap<>(wpUser.getAttributes());
    }

    
    /** 
     * @param realm
     * @param name
     * @param value
     * @return List<String>
     */
    @Override
    public List<String> getUsersByUserAttribute(RealmModel realm, String name, String value) {

        log.infov("get users by user attribute: realm={0} name={1} value={2}", realm.getId(), value);

        return repository.findUsersByAttribute(name, value, 0, -1);
    }




    @Override
    public UserModel addUser(RealmModel realm, String username) {

        log.infov("add user: realm={0} username={1}", realm.getId(), username);

        // this is not supported
        return null;
    }

    
    /** 
     * @param realm
     * @param user
     * @return boolean
     */
    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {

        log.infov("remove user: realm={0} username={1}", realm.getId(), user.getUsername());

        // this is not supported
        return false;
    }
    
}
