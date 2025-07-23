package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.service.CursoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/cursos")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CursoResource {

    CursoService cursoService;

    public CursoResource(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GET
    @RolesAllowed({"ADMIN", "COORDENADOR", "ALUNO", "PROFESSOR"}) // Todos podem ver os cursos
    public List<Curso> listarTodosCursos() {
        return cursoService.listarTodosCursos();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "ALUNO", "PROFESSOR"})
    public Response buscarCursoPorId(@PathParam("id") Long id) {
        return cursoService.buscarCursoPorId(id)
                .map(curso -> Response.ok(curso).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/codigo/{codigo}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "ALUNO", "PROFESSOR"})
    public Response buscarCursoPorCodigo(@PathParam("codigo") String codigo) {
        return cursoService.buscarCursoPorCodigo(codigo)
                .map(curso -> Response.ok(curso).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Admin e Coordenador podem criar cursos
    public Response criarCurso(Curso curso) {
        try {
            Curso novoCurso = cursoService.criarCurso(curso);
            return Response.created(UriBuilder.fromResource(CursoResource.class).path(novoCurso.id.toString()).build())
                    .entity(novoCurso)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Admin e Coordenador podem atualizar cursos
    public Response atualizarCurso(@PathParam("id") Long id, Curso cursoAtualizado) {
        try {
            Curso curso = cursoService.atualizarCurso(id, cursoAtualizado);
            return Response.ok(curso).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Apenas administradores podem deletar cursos
    public Response deletarCurso(@PathParam("id") Long id) {
        boolean deletado = cursoService.deletarCurso(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}