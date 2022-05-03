package com.swinburne.keycloak.userstorage.wordpress.client;

import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;

import com.swinburne.keycloak.userstorage.wordpress.client.pojo.AccessTokenRequest;
import com.swinburne.keycloak.userstorage.wordpress.client.pojo.AccessTokenResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

@JBossLog
public class WpClientProvider  {

    //private final AtomicReference<ExpiringAccessToken> accessTokenResponseHolder = new AtomicReference<>();

    private final String authServerUrl;

    private final WpRestKeycloakClient remoteKeycloakClient;


    public WpClientProvider(ComponentModel componentModel, Function<ComponentModel, ResteasyClient> clientFactory) {
        this.authServerUrl = "http://localhost:80/";//componentModel.get("authServerUrl", "http://localhost:8080/auth");
        this.remoteKeycloakClient = createRestWpClient(clientFactory.apply(componentModel));
    }

    private WpRestKeycloakClient createRestWpClient(ResteasyClient client) {
        ResteasyWebTarget webTarget = client.target(UriBuilder.fromPath(this.authServerUrl).toString());
        webTarget.register(new AccessTokenInterceptor(this::getAccessToken));
        return webTarget.proxy(WpRestKeycloakClient.class);
    }

    public WpRestKeycloakClient getRemoteKeycloakClient() {
        return remoteKeycloakClient;
    }

    private String getAccessToken() {
        AccessTokenResponse atr = remoteKeycloakClient.getToken(new AccessTokenRequest("admin", "admin"));
        return atr.getData().getToken();

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
