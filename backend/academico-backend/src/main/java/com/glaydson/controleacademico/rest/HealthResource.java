package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.service.UserService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.HttpURLConnection;
import java.net.URL;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @ConfigProperty(name = "keycloak.admin.server-url")
    String keycloakServerUrl;

    @ConfigProperty(name = "keycloak.admin.realm")
    String realm;

    @GET
    @Path("/keycloak")
    public Response checkKeycloak() {
        try {
            // First, check if Keycloak server is reachable
            URL url = new URL(keycloakServerUrl + "/realms/" + realm);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                return Response.ok()
                    .entity("{\"status\": \"UP\", \"keycloak\": \"REACHABLE\", \"realm\": \"" + realm + "\"}")
                    .build();
            } else {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"status\": \"DOWN\", \"keycloak\": \"UNREACHABLE\", \"responseCode\": " + responseCode + "}")
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("{\"status\": \"DOWN\", \"keycloak\": \"CONNECTION_FAILED\", \"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }
}
