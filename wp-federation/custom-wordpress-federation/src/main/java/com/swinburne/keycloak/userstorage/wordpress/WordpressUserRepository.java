package com.swinburne.keycloak.userstorage.wordpress;

import java.util.Collections;
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

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import lombok.extern.jbosslog.JBossLog;

/**
 * The main interaction with the WP REST API
 */
@JBossLog
 class WordpressUserRepository {

    //public static final WordpressRole wp_ADMIN_ROLE = new WordpressRole("1", "wp-admin", "wp Admin Role");
    //public static final WordpressRole wp_USER_ROLE = new WordpressRole("2", "wp-user", "wp User Role");

    private  List<WordpressUser> wpUsers;
    private  Map<String, Set<WordpressRole>> userRoles;
    
    private  List<WpClientProvider> remoteRestClients;
    
    public WordpressUserRepository(List<WpClientProvider> restClients) {
        this.wpUsers               = Collections.<WordpressUser>emptyList();
        this.userRoles             = Collections.<String, Set<WordpressRole>>emptyMap();
        this.remoteRestClients     = restClients;
        
        ResteasyProviderFactory.getInstance().registerProvider(RestExceptionMapper.class);
        collectAllUsersFromRemote();
    }

    public void collectAllUsersFromRemote() {
        for (WpClientProvider client: this.remoteRestClients) {
            log.infov("@mb collectAllUsersFromRemote token: {0}",client.getAccessToken());
        }
    }

    /** 
     * @return List<WordpressUser>
     */
    public List<WordpressUser> getAllUsers() {
        this.collectAllUsersFromRemote();
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
        this.collectAllUsersFromRemote();

        return wpUsers.stream().filter(wpUser -> wpUser.getId().equals(id)).findFirst().orElse(null);
    }

    
    /** 
     * @param username
     * @return WordpressUser
     */
    public WordpressUser findUserByUsernameOrEmail(String username) {
        this.collectAllUsersFromRemote();

        return wpUsers.stream()
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
        this.collectAllUsersFromRemote();

        return paginated(wpUsers.stream()
                .filter(wpUser -> wpUser.getUsername().contains(query)
                        || wpUser.getEmail().contains(query)
                        || wpUser.getFirstName().contains(query)
                        || wpUser.getLastName().contains(query)), firstResult, maxResult)
                .collect(Collectors.toList());
    }

    
    /** 
     * @param username
     * @param password
     * @return boolean
     */
    public boolean validateCredentials(String username, String password) {
        WordpressUser user = findUserByUsernameOrEmail(username);
        return user.getPassword().equals(password);
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

        WordpressUser user = findUserByUsernameOrEmail(username);
        return getGlobalRolesByUserId(user.getId());
    }


    
    /** 
     * @param userId
     * @return Set<WordpressRole>
     */
    public Set<WordpressRole> getGlobalRolesByUserId(String userId) {
        return userRoles.get(userId);
    }

    
    /** 
     * @param clientId
     * @param userId
     * @return Set<WordpressRole>
     */
    public Set<WordpressRole> getClientRolesByUserId(String clientId, String userId) {
        return userRoles.get(clientId + ":" + userId);
    }

    
    /** 
     * @param name
     * @param value
     * @param firstResult
     * @param maxResult
     * @return List<String>
     */
    public List<String> findUsersByAttribute(String name, String value, int firstResult, int maxResult) {
        return paginated(wpUsers.stream()
                .filter(u -> u.getAttribute(name).contains(value))
                .map(WordpressUser::getId), firstResult, maxResult)
                .collect(Collectors.toList());
    }

    
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
