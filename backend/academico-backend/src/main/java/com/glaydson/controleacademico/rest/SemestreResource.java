package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Semestre;
import com.glaydson.controleacademico.rest.dto.SemestreRequestDTO;
import com.glaydson.controleacademico.service.SemestreService;
import com.glaydson.controleacademico.rest.dto.SemestreResponseDTO; // Importa DTO de saída
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.validation.Valid; // Para validação de DTOs

import java.util.List;
import java.util.stream.Collectors;

@Path("/semestres")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SemestreResource {

    SemestreService semestreService;

    public SemestreResource(SemestreService semestreService) {
        this.semestreService = semestreService;
    }

    @GET
    @RolesAllowed({ "COORDENADOR", "ALUNO", "PROFESSOR"}) // Todos podem ver os semestres
    public List<SemestreResponseDTO> listarTodosSemestres() { // Retorna lista de DTOs
        return semestreService.listarTodosSemestres().stream()
                .map(SemestreResponseDTO::new) // Converte entidade para DTO
                .toList();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "COORDENADOR", "ALUNO", "PROFESSOR"})
    public Response buscarSemestrePorId(@PathParam("id") Long id) {
        return semestreService.buscarSemestrePorId(id)
                .map(semestre -> Response.ok(new SemestreResponseDTO(semestre)).build()) // Converte entidade para DTO
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/ano/{ano}/periodo/{periodo}")
    @RolesAllowed({ "COORDENADOR", "ALUNO", "PROFESSOR"})
    public Response buscarSemestrePorAnoPeriodo(@PathParam("ano") Integer ano, @PathParam("periodo") String periodo) {
        return semestreService.buscarSemestrePorAnoPeriodo(ano, periodo)
                .map(semestre -> Response.ok(new SemestreResponseDTO(semestre)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({"COORDENADOR"})
    public Response criarSemestre(@Valid SemestreRequestDTO semestreDto) {
        try {
            Semestre novoSemestre = semestreService.criarSemestre(semestreDto);
            SemestreResponseDTO responseDto = new SemestreResponseDTO(novoSemestre);
            return Response.created(UriBuilder.fromResource(SemestreResource.class).path(responseDto.id.toString()).build())
                    .entity(responseDto)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"COORDENADOR"}) // Apenas coordenadores podem atualizar semestres
    public Response atualizarSemestre(@PathParam("id") Long id, @Valid SemestreRequestDTO semestreDto) { // Recebe DTO para atualização
        try {
            Semestre semestre = semestreService.atualizarSemestre(id, semestreDto); // Serviço recebe DTO
            SemestreResponseDTO responseDto = new SemestreResponseDTO(semestre); // Converte entidade atualizada para DTO
            return Response.ok(responseDto).build(); // Retorna DTO
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("COORDENADOR")
    public Response deletarSemestre(@PathParam("id") Long id) {
        boolean deletado = semestreService.deletarSemestre(id);
        if (deletado) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}