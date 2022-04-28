package com.swinburne.keycloak.userstorage.client;

import org.keycloak.OAuth2Constants;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

public interface WordpressRestClient {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/realms/{realm}/protocol/openid-connect/token")
    AccessTokenResponse getToken(@PathParam("realm") String realm, //
                                 @FormParam(OAuth2Constants.CLIENT_ID) String clientId, //
                                 @FormParam(OAuth2Constants.CLIENT_SECRET) String clientSecret, //
                                 @FormParam(OAuth2Constants.SCOPE) String scope, //
                                 @FormParam(OAuth2Constants.GRANT_TYPE) String grantType //
    );
}
