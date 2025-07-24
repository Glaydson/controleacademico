package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Professor;
import com.glaydson.controleacademico.service.ProfessorService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/professores")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfessorResource {

    ProfessorService professorService;

    public ProfessorResource(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GET
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR"}) // Professores podem ver outros professores
    public List<Professor> listarTodosProfessores() {
        return professorService.listarTodosProfessores();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR"})
    public Response buscarProfessorPorId(@PathParam("id") Long id) {
        return professorService.buscarProfessorPorId(id)
                .map(professor -> Response.ok(professor).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/matricula/{matricula}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR"})
    public Response buscarProfessorPorRegistro(@PathParam("matricula") String registro) {
        return professorService.buscarProfessorPorRegistro(registro)
                .map(professor -> Response.ok(professor).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed("ADMIN") // Apenas administradores criam professores
    public Response criarProfessor(Professor professor) {
        try {
            Professor novoProfessor = professorService.criarProfessor(professor);
            return Response.created(UriBuilder.fromResource(ProfessorResource.class).path(novoProfessor.getId().toString()).build())
                    .entity(novoProfessor)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Apenas administradores atualizam professores
    public Response atualizarProfessor(@PathParam("id") Long id, Professor professorAtualizado) {
        try {
            Professor professor = professorService.atualizarProfessor(id, professorAtualizado);
            return Response.ok(professor).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Apenas administradores deletam professores
    public Response deletarProfessor(@PathParam("id") Long id) {
        boolean deletado = professorService.deletarProfessor(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
