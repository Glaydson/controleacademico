package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.MatrizCurricular;
import com.glaydson.controleacademico.service.MatrizCurricularService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/matrizes-curriculares")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MatrizCurricularResource {

    MatrizCurricularService matrizCurricularService;

    public MatrizCurricularResource(MatrizCurricularService matrizCurricularService) {
        this.matrizCurricularService = matrizCurricularService; // Construtor para injeção de dependência
    }

    @GET
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR", "ALUNO"}) // Todos podem ver as matrizes
    public List<MatrizCurricular> listarTodasMatrizesCurriculares() {
        return matrizCurricularService.listarTodasMatrizesCurriculares();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR", "ALUNO"})
    public Response buscarMatrizCurricularPorId(@PathParam("id") Long id) {
        return matrizCurricularService.buscarMatrizCurricularPorId(id)
                .map(matriz -> Response.ok(matriz).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Admin e Coordenador podem criar matrizes
    public Response criarMatrizCurricular(MatrizCurricular matrizCurricular) {
        try {
            MatrizCurricular novaMatriz = matrizCurricularService.criarMatrizCurricular(matrizCurricular);
            return Response
                    .created(UriBuilder.fromResource(MatrizCurricularResource.class)
                            .path(novaMatriz.id.toString()).build())
                    .entity(novaMatriz)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "COORDENADOR"}) // Admin e Coordenador podem atualizar matrizes
    public Response atualizarMatrizCurricular(@PathParam("id") Long id, MatrizCurricular matrizAtualizada) {
        try {
            MatrizCurricular matriz = matrizCurricularService.atualizarMatrizCurricular(id, matrizAtualizada);
            return Response.ok(matriz).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Apenas administradores podem deletar matrizes
    public Response deletarMatrizCurricular(@PathParam("id") Long id) {
        boolean deletado = matrizCurricularService.deletarMatrizCurricular(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}