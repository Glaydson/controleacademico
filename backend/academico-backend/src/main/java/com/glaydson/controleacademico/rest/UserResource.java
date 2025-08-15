package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.rest.dto.UserCreateRequestDTO;
import com.glaydson.controleacademico.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @RolesAllowed({"ADMIN", "COORDENADOR"})
    public Response getAllUsers() {
        System.out.println("=== UserResource.getAllUsers() called - JWT validation passed ===");
        try {
            System.out.println("=== About to call userService.getAllUsers() ===");
            var users = userService.getAllUsers();
            System.out.println("=== userService.getAllUsers() completed successfully ===");
            return Response.ok(users).build();
        } catch (Exception e) {
            System.err.println("=== Exception in UserResource.getAllUsers(): " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(java.util.Map.of("message", "Error retrieving users: " + e.getMessage(), "success", false))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response getUserById(@PathParam("id") String id) {
        try {
            var user = userService.getUserById(id);
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of("message", "User not found: " + e.getMessage(), "success", false))
                    .build();
        }
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response createUser(UserCreateRequestDTO requestDTO) {
        try {
            var createdUser = userService.createUser(requestDTO);
            return Response.status(Response.Status.CREATED)
                    .entity(createdUser)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("message", e.getMessage(), "success", false))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response updateUser(@PathParam("id") String id, UserCreateRequestDTO requestDTO) {
        try {
            userService.updateUser(id, requestDTO);
            return Response.ok(java.util.Map.of("message", "User updated successfully.", "success", true)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("message", "Error updating user: " + e.getMessage(), "success", false))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response deleteUser(@PathParam("id") String id) {
        try {
            userService.deleteUser(id);
            return Response.ok(java.util.Map.of("message", "User deleted successfully.", "success", true)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of("message", "Error deleting user: " + e.getMessage(), "success", false))
                    .build();
        }
    }

    @GET
    @Path("/admin-roles")
    @RolesAllowed({"ADMIN"})
    public Response getAvailableAdminRoles() {
        try {
            var roles = userService.getAvailableAdminRoles();
            return Response.ok(roles).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(java.util.Map.of("message", "Error retrieving admin roles: " + e.getMessage(), "success", false))
                    .build();
        }
    }

    @POST
    @Path("/sync-keycloak")
    public Response syncKeycloakUsers() {
        try {
            userService.syncKeycloakUsersToBackend();
            return Response.ok(java.util.Map.of("message", "Keycloak users synchronized to backend database successfully", "success", true)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(java.util.Map.of("message", "Error synchronizing users: " + e.getMessage(), "success", false))
                    .build();
        }
    }
}
