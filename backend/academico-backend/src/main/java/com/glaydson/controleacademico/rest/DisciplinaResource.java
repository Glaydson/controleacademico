package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.rest.dto.DisciplinaRequestDTO;
import com.glaydson.controleacademico.rest.dto.DisciplinaResponseDTO;
import com.glaydson.controleacademico.service.DisciplinaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
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
        this.disciplinaService = disciplinaService;
    }

    @GET
    @RolesAllowed({ "COORDENADOR", "PROFESSOR", "ALUNO"})
    public List<DisciplinaResponseDTO> listarTodasDisciplinas() {
        return disciplinaService.listarTodasDisciplinas().stream()
                .map(DisciplinaResponseDTO::new)
                .toList();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "COORDENADOR", "PROFESSOR", "ALUNO"})
    public Response buscarDisciplinaPorId(@PathParam("id") Long id) {
        return disciplinaService.buscarDisciplinaPorId(id)
                .map(disciplina -> Response.ok(new DisciplinaResponseDTO(disciplina)).build()) // Convert entity to DTO
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/codigo/{codigo}")
    @RolesAllowed({ "COORDENADOR", "PROFESSOR", "ALUNO"})
    public Response buscarDisciplinaPorCodigo(@PathParam("codigo") String codigo) {
        return disciplinaService.buscarDisciplinaPorCodigo(codigo)
                .map(disciplina -> Response.ok(new DisciplinaResponseDTO(disciplina)).build()) // Convert entity to DTO
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({ "COORDENADOR"})
    public Response criarDisciplina(@Valid DisciplinaRequestDTO disciplinaDto) {
        try {
            Disciplina novaDisciplina = disciplinaService.criarDisciplina(disciplinaDto);
            DisciplinaResponseDTO responseDto = new DisciplinaResponseDTO(novaDisciplina);
            return Response.created(UriBuilder.fromResource(DisciplinaResource.class).path(responseDto.id.toString()).build())
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
    @RolesAllowed({ "COORDENADOR"})
    public Response atualizarDisciplina(@PathParam("id") Long id, @Valid DisciplinaRequestDTO disciplinaDto) {
        try {
            Disciplina disciplina = disciplinaService.atualizarDisciplina(id, disciplinaDto);
            DisciplinaResponseDTO responseDto = new DisciplinaResponseDTO(disciplina);
            return Response.ok(responseDto).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("COORDENADOR")
    public Response deletarDisciplina(@PathParam("id") Long id) {
        boolean deletado = disciplinaService.deletarDisciplina(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}