package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Semestre;
import com.glaydson.controleacademico.service.SemestreService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/semestres")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SemestreResource {

    SemestreService semestreService;

    public SemestreResource(SemestreService semestreService) {
        this.semestreService = semestreService; // Construtor para injeção de dependência
    }

    @GET
    @RolesAllowed({"ADMIN", "COORDENADOR", "ALUNO", "PROFESSOR"}) // Todos podem ver os semestres
    public List<Semestre> listarTodosSemestres() {
        return semestreService.listarTodosSemestres();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "ALUNO", "PROFESSOR"})
    public Response buscarSemestrePorId(@PathParam("id") Long id) {
        return semestreService.buscarSemestrePorId(id)
                .map(semestre -> Response.ok(semestre).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/nome/{nome}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "ALUNO", "PROFESSOR"})
    public Response buscarSemestrePorNome(@PathParam("nome") String nome) {
        return semestreService.buscarSemestrePorNome(nome)
                .map(semestre -> Response.ok(semestre).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Admin e Coordenador podem criar semestres
    public Response criarSemestre(Semestre semestre) {
        try {
            Semestre novoSemestre = semestreService.criarSemestre(semestre);
            return Response.created(UriBuilder.fromResource(SemestreResource.class).path(novoSemestre.id.toString()).build())
                    .entity(novoSemestre)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Admin e Coordenador podem atualizar semestres
    public Response atualizarSemestre(@PathParam("id") Long id, Semestre semestreAtualizado) {
        try {
            Semestre semestre = semestreService.atualizarSemestre(id, semestreAtualizado);
            return Response.ok(semestre).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Apenas administradores podem deletar semestres
    public Response deletarSemestre(@PathParam("id") Long id) {
        boolean deletado = semestreService.deletarSemestre(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
