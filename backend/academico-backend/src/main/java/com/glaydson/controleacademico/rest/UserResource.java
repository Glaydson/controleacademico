package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.rest.dto.UserCreateRequestDTO;
import com.glaydson.controleacademico.service.UserService;
import com.glaydson.controleacademico.rest.dto.UserResponseDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @RolesAllowed("ADMIN")
    public Response getAllUsers() {
        try {
            var users = userService.getAllUsers();
            return Response.ok(users).build();
        } catch (Exception e) {
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
}
