package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.MatrizCurricular;
import com.glaydson.controleacademico.rest.dto.MatrizCurricularRequestDTO;
import com.glaydson.controleacademico.rest.dto.MatrizCurricularResponseDTO;
import com.glaydson.controleacademico.service.MatrizCurricularService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;
import java.util.stream.Collectors;

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
    @RolesAllowed({ "COORDENADOR", "PROFESSOR", "ALUNO"})
    public List<MatrizCurricularResponseDTO> listarTodasMatrizesCurriculares() {
        return matrizCurricularService.listarTodasMatrizesCurriculares().stream()
                .map(MatrizCurricularResponseDTO::new) // Converte cada entidade para DTO de resposta
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"COORDENADOR", "PROFESSOR", "ALUNO"})
    public Response buscarMatrizCurricularPorId(@PathParam("id") Long id) {
        return matrizCurricularService.buscarMatrizCurricularPorId(id)
                .map(matriz -> Response.ok(new MatrizCurricularResponseDTO(matriz)).build()) // Converte para DTO de resposta
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }


    @POST
    @RolesAllowed({ "COORDENADOR"})
    public Response criarMatrizCurricular(@Valid MatrizCurricularRequestDTO matrizDto) { // Recebe DTO de criação
        try {
            MatrizCurricular novaMatriz = matrizCurricularService.criarMatrizCurricular(matrizDto); // Passa DTO para o service
            // Converte a entidade persistida para DTO de resposta antes de retornar
            MatrizCurricularResponseDTO responseDto = new MatrizCurricularResponseDTO(novaMatriz);
            return Response.created(UriBuilder.fromResource(MatrizCurricularResource.class).path(responseDto.id.toString()).build())
                    .entity(responseDto)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"COORDENADOR"})
    public Response atualizarMatrizCurricular(@PathParam("id") Long id, @Valid MatrizCurricularRequestDTO matrizDto) { // Recebe DTO de atualização
        try {
            MatrizCurricular matriz = matrizCurricularService.atualizarMatrizCurricular(id, matrizDto); // Passa DTO para o service
            // Converte a entidade atualizada para DTO de resposta antes de retornar
            MatrizCurricularResponseDTO responseDto = new MatrizCurricularResponseDTO(matriz);
            return Response.ok(responseDto).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("COORDENADOR") // Apenas coordenadores podem deletar matrizes
    public Response deletarMatrizCurricular(@PathParam("id") Long id) {
        boolean deletado = matrizCurricularService.deletarMatrizCurricular(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}