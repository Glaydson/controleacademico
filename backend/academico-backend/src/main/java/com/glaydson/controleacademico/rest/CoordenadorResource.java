package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Coordenador;
import com.glaydson.controleacademico.service.CoordenadorService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/coordenadores")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordenadorResource {


    CoordenadorService coordenadorService;

    public CoordenadorResource(CoordenadorService coordenadorService) {
        this.coordenadorService = coordenadorService; // Injeção de dependência
    }

    @GET
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Coordenadores podem ver outros coordenadores
    public List<Coordenador> listarTodosCoordenadores() {
        return coordenadorService.listarTodosCoordenadores();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR"})
    public Response buscarCoordenadorPorId(@PathParam("id") Long id) {
        return coordenadorService.buscarCoordenadorPorId(id)
                .map(coordenador -> Response.ok(coordenador).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/registro/{registro}")
    @RolesAllowed({"ADMIN", "COORDENADOR"})
    public Response buscarCoordenadorPorRegistro(@PathParam("registro") String registro) {
        return coordenadorService.buscarCoordenadorPorRegistro(registro)
                .map(coordenador -> Response.ok(coordenador).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed("ADMIN") // Apenas administradores criam coordenadores
    public Response criarCoordenador(Coordenador coordenador) {
        try {
            Coordenador novoCoordenador = coordenadorService.criarCoordenador(coordenador);
            return Response.created(UriBuilder.fromResource(CoordenadorResource.class).path(novoCoordenador.getId().toString()).build())
                    .entity(novoCoordenador)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Apenas administradores atualizam coordenadores
    public Response atualizarCoordenador(@PathParam("id") Long id, Coordenador coordenadorAtualizado) {
        try {
            Coordenador coordenador = coordenadorService.atualizarCoordenador(id, coordenadorAtualizado);
            return Response.ok(coordenador).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Apenas administradores deletam coordenadores
    public Response deletarCoordenador(@PathParam("id") Long id) {
        boolean deletado = coordenadorService.deletarCoordenador(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}