package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.service.DisciplinaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/disciplinas")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DisciplinaResource {

    DisciplinaService disciplinaService;

    public DisciplinaResource(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService; // Construtor para injeção de dependência
    }

    @GET
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR", "ALUNO"}) // Todos podem ver as disciplinas
    public List<Disciplina> listarTodasDisciplinas() {
        return disciplinaService.listarTodasDisciplinas();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR", "ALUNO"})
    public Response buscarDisciplinaPorId(@PathParam("id") Long id) {
        return disciplinaService.buscarDisciplinaPorId(id)
                .map(disciplina -> Response.ok(disciplina).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/codigo/{codigo}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR", "ALUNO"})
    public Response buscarDisciplinaPorCodigo(@PathParam("codigo") String codigo) {
        return disciplinaService.buscarDisciplinaPorCodigo(codigo)
                .map(disciplina -> Response.ok(disciplina).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Admin e Coordenador podem criar disciplinas
    public Response criarDisciplina(Disciplina disciplina) {
        try {
            Disciplina novaDisciplina = disciplinaService.criarDisciplina(disciplina);
            return Response.created(UriBuilder.fromResource(DisciplinaResource.class).path(novaDisciplina.id.toString()).build())
                    .entity(novaDisciplina)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Admin e Coordenador podem atualizar disciplinas
    public Response atualizarDisciplina(@PathParam("id") Long id, Disciplina disciplinaAtualizada) {
        try {
            Disciplina disciplina = disciplinaService.atualizarDisciplina(id, disciplinaAtualizada);
            return Response.ok(disciplina).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Apenas administradores podem deletar disciplinas
    public Response deletarDisciplina(@PathParam("id") Long id) {
        boolean deletado = disciplinaService.deletarDisciplina(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}