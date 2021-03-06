package com.swinburne.keycloak.userstorage.wordpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.swinburne.keycloak.userstorage.wordpress.client.RestExceptionMapper;
import com.swinburne.keycloak.userstorage.wordpress.client.WpClientProvider;
import com.swinburne.keycloak.userstorage.wordpress.client.WpRestKeycloakClient;
import com.swinburne.keycloak.userstorage.wordpress.client.pojo.AccessTokenRequest;
import com.swinburne.keycloak.userstorage.wordpress.client.pojo.AccessTokenResponse;
import com.swinburne.keycloak.userstorage.wordpress.client.pojo.WordpressUser;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import lombok.extern.jbosslog.JBossLog;

/**
 * The main interaction with the WP REST API
 */
@JBossLog
 class WordpressUserRepository {

    private  List<WordpressUser> wpUsers;
    private  Map<String, Set<WordpressRole>> userRoles;
    
    private  List<WpClientProvider> remoteRestClients;
    
    public WordpressUserRepository(List<WpClientProvider> restClients) {
        this.wpUsers               = Collections.<WordpressUser>emptyList();
        this.userRoles             = Collections.<String, Set<WordpressRole>>emptyMap();
        this.remoteRestClients     = restClients;
        
        ResteasyProviderFactory.getInstance().registerProvider(RestExceptionMapper.class);
    }

    /** 
     * @return List<WordpressUser>
     */
    public List<WordpressUser> getAllUsers() {    
        log.infof("getAllUsers");    
        return wpUsers;
    }

    
    /** 
     * @return int
     */
    public int getUsersCount() {
        return wpUsers.size();
    }

    
    /** 
     * @param id
     * @return WordpressUser
     */
    public WordpressUser findUserById(String id) {
        log.infov("findUserById {0}", id);

        try {
            int instance_id = 0; //only one 
            
            String user_id = id;
            WpClientProvider c = this.remoteRestClients.get(instance_id);

            if (c == null) {
                log.warnf("cannot find WpClientProvider!");
                return null;
            }
            if (c.getAccessToken() == null)
                return null;
                
            WordpressUser user = c.getRemoteKeycloakClient().getUserById(user_id,"edit");
            return user;        
        } catch (Exception e) {
            log.warnv("{0}",e);
        }

        return null;        
    }

    
    /** 
     * @param username
     * @return WordpressUser
     */
    public WordpressUser findUserByUsernameOrEmail(String username) {
        log.infov("findUserByUsernameOrEmail {0}", username);
        return findUsers(username,0,-1).stream()
                .filter(wpUser -> wpUser.getUsername().equalsIgnoreCase(username) || wpUser.getEmail().equalsIgnoreCase(username))
                .findFirst().orElse(null);
    }

    
    /** 
     * @param query
     * @param firstResult
     * @param maxResult
     * @return List<WordpressUser>
     */
    public List<WordpressUser> findUsers(String query, int firstResult, int maxResult) {

        log.infof("findUsers {0}", query);

        ArrayList<WordpressUser> users = new ArrayList<WordpressUser>();

        int instance_id = 0;
        for (WpClientProvider c: this.remoteRestClients ) {
            if (c.getAccessToken() != null) {
                List<WordpressUser> rusers = c.getRemoteKeycloakClient().searchUsers(query,"edit");
                log.infov("remote users retrieved: {0}", rusers);
                users.addAll(rusers);
                instance_id++;
            }
        }

        return users;
    }

    
    /** 
     * @param username
     * @param password
     * @return boolean
     */
    public boolean validateCredentials(String username, String password) {
        log.infof("validateCredentials {0}", username);
        WordpressUser user = findUserByUsernameOrEmail(username);

        for (WpClientProvider c: this.remoteRestClients ) {
            try{
                AccessTokenResponse rs = c.getRemoteKeycloakClient().getToken(new AccessTokenRequest(username, password));
                if (rs.getData().getToken() != null) {
                    log.infov("user {0} password validated", username);
                    return true;
                }

            } catch (Exception ex) {}
        }

        log.infov("user {0} password NOT validated", username);
        return false;
    }

    
    /** 
     * @param username
     * @param password
     * @return boolean
     */
    public boolean updateCredentials(String username, String password) {
        findUserByUsernameOrEmail(username).setPassword(password);
        return true;
    }

    
    /** 
     * @param username
     * @return Set<WordpressRole>
     */
    public Set<WordpressRole> getRoles(String username) {
        log.infof("getRoles {0}", username);

        WordpressUser user = findUserByUsernameOrEmail(username);
        return getGlobalRolesByUserId(user.getId());
    }


    
    /** 
     * @param userId
     * @return Set<WordpressRole>
     */
    public Set<WordpressRole> getGlobalRolesByUserId(String userId) {
        log.infof("getGlobalRolesByUserId {0}", userId);
        WordpressUser user = findUserById(userId);
        return user.getRolesAsWordpressRole();
    }

    
    /** 
     * @param clientId
     * @param userId
     * @return Set<WordpressRole>
     */
    public Set<WordpressRole> getClientRolesByUserId(String clientId, String userId) {
        return userRoles.get(clientId + ":" + userId);
    }

    
    // /** 
    //  * @param name
    //  * @param value
    //  * @param firstResult
    //  * @param maxResult
    //  * @return List<String>
    //  */
    // public List<String> findUsersByAttribute(String name, String value, int firstResult, int maxResult) {
    //     return paginated(wpUsers.stream()
    //             .filter(u -> u.getAttribute(name).contains(value))
    //             .map(WordpressUser::getId), firstResult, maxResult)
    //             .collect(Collectors.toList());
    // }

    
    /** 
     * @param stream
     * @param firstResult
     * @param maxResult
     * @return Stream<T>
     */
    protected <T> Stream<T> paginated(Stream<T> stream, int firstResult, int maxResult) {

        Stream result = stream.skip(firstResult);

        if (maxResult != -1) {
            result = result.limit(maxResult);
        }

        return result;
    }
}
