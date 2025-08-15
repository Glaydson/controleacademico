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
                    .entity("Error retrieving users: " + e.getMessage())
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
                    .entity("User not found: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response createUser(UserCreateRequestDTO requestDTO) {
        try {
            userService.createUser(requestDTO);
            return Response.status(Response.Status.CREATED)
                    .entity("User created successfully.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response updateUser(@PathParam("id") String id, UserCreateRequestDTO requestDTO) {
        try {
            userService.updateUser(id, requestDTO);
            return Response.ok("User updated successfully.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error updating user: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response deleteUser(@PathParam("id") String id) {
        try {
            userService.deleteUser(id);
            return Response.ok("User deleted successfully.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error deleting user: " + e.getMessage())
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
                    .entity("Error retrieving admin roles: " + e.getMessage())
                    .build();
        }
    }
}
