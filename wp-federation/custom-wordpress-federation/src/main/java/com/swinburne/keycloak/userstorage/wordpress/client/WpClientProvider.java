package com.swinburne.keycloak.userstorage.wordpress.client;

import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.services.ErrorResponseException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import com.swinburne.keycloak.userstorage.wordpress.client.pojo.AccessTokenRequest;
import com.swinburne.keycloak.userstorage.wordpress.client.pojo.AccessTokenResponse;
import com.swinburne.keycloak.userstorage.wordpress.client.RestExceptionMapper;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

@JBossLog
public class WpClientProvider  {

    private String authServerUrl;
    private String adminUsername;
    private String adminPassword;
    private String accessToken; //does not expire
    private final WpRestKeycloakClient remoteKeycloakClient;


    public WpClientProvider(ComponentModel componentModel, String authServerUrl, 
                            String adminUsername, String adminPassword,
                            Function<ComponentModel, ResteasyClient> clientFactory) {
        this.authServerUrl = authServerUrl;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.remoteKeycloakClient = createRestWpClient(clientFactory.apply(componentModel));
    }

    private WpRestKeycloakClient createRestWpClient(ResteasyClient client) {
        ResteasyWebTarget webTarget = client.target(UriBuilder.fromPath(this.authServerUrl));
        webTarget.register(new AccessTokenInterceptor(this::getAccessToken));
        return webTarget.proxy(WpRestKeycloakClient.class);
    }

    public WpRestKeycloakClient getRemoteKeycloakClient() {
        return remoteKeycloakClient;
    }

    /**
     * 
     * @return Wordpress JWT
     */

    public String getAccessToken() {
        AccessTokenResponse atr = null;
        try {
            if (this.accessToken == null) {
                atr = remoteKeycloakClient.getToken(new AccessTokenRequest(this.adminUsername, this.adminPassword));
                this.accessToken = atr.getData().getToken();
            }
        } catch (Exception ex) {
            log.infov("Failed to get access token response:{0}", ex.getMessage());
        }
        return this.accessToken;
    }

    private static class AccessTokenInterceptor implements ClientRequestFilter {
        private final Supplier<String> accessTokenSupplier;

        public AccessTokenInterceptor(Supplier<String> accessTokenSupplier) {
            this.accessTokenSupplier = accessTokenSupplier;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            String requestPath = requestContext.getUri().getPath();

            // token request
            if (requestPath.endsWith("/token")) {
                return;
            }


            String accessToken = accessTokenSupplier.get();
            if (accessToken == null) {
                return;
            }

            requestContext.getHeaders().addFirst(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
    }
}
