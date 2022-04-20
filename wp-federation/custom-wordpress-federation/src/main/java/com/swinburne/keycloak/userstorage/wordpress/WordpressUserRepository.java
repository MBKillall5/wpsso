package com.swinburne.keycloak.userstorage.wordpress;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main interaction with the WP REST API
 */
class WordpressUserRepository {

    public static final WordpressRole wp_ADMIN_ROLE = new WordpressRole("1", "wp-admin", "wp Admin Role");
    public static final WordpressRole wp_USER_ROLE = new WordpressRole("2", "wp-user", "wp User Role");
    public static final WordpressRole wp_CLIENT_ROLE_TEST_CLIENT_MANAGER = new WordpressRole("1001", "wp-test-client-manager", "wp Test Client Manager Role");

    private final List<WordpressUser> wpUsers;

    private final Map<String, Set<WordpressRole>> userRoles;

    public WordpressUserRepository() {
        wpUsers = List.of(
                new WordpressUser("1", "user1", "secret", "First1", "Last1",
                        Map.of("attribute1", List.of("value1_1")), true),
                new WordpressUser("2", "user2", "secret", "First2", "Last2",
                        Map.of("attribute1", List.of("value1_2")), true),
                new WordpressUser("3", "user3", "secret", "First3", "Last3",
                        Map.of("attribute1", List.of("value1_3")), true),
                new WordpressUser("4", "user4", "secret", "First4", "Last4",
                        Map.of("attribute1", List.of("value1_4")), false)
        );

        userRoles = Map.ofEntries(
                // global user roles
                Map.entry("1", Set.of(wp_ADMIN_ROLE, wp_USER_ROLE)),
                Map.entry("2", Set.of(wp_ADMIN_ROLE, wp_USER_ROLE)),
                Map.entry("3", Set.of(wp_USER_ROLE)),
                Map.entry("4", Set.of(wp_USER_ROLE)),

                // client user roles
                Map.entry("test-client:1", Set.of(wp_CLIENT_ROLE_TEST_CLIENT_MANAGER))
        );
    }

    
    /** 
     * @return List<WordpressUser>
     */
    public List<WordpressUser> getAllUsers() {
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
        return wpUsers.stream().filter(wpUser -> wpUser.getId().equals(id)).findFirst().orElse(null);
    }

    
    /** 
     * @param username
     * @return WordpressUser
     */
    public WordpressUser findUserByUsernameOrEmail(String username) {
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
