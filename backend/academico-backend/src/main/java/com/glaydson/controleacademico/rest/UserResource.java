package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.rest.dto.UserCreateRequestDTO;
import com.glaydson.controleacademico.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

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
}
