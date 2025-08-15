package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.rest.dto.CursoRequestDTO;
import com.glaydson.controleacademico.rest.dto.CursoResponseDTO;
import com.glaydson.controleacademico.service.CursoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
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
    @RolesAllowed({ "COORDENADOR", "ALUNO", "PROFESSOR"})
    public List<CursoResponseDTO> listarTodosCursos() {
        return cursoService.listarTodosCursos().stream()
                .map(CursoResponseDTO::new)
                .toList();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "COORDENADOR", "ALUNO", "PROFESSOR"})
    public Response buscarCursoPorId(@PathParam("id") Long id) {
        return cursoService.buscarCursoPorId(id)
                .map(curso -> Response.ok(new CursoResponseDTO(curso)).build()) // Convert entity to DTO
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/codigo/{codigo}")
    @RolesAllowed({ "COORDENADOR", "ALUNO", "PROFESSOR"})
    public Response buscarCursoPorCodigo(@PathParam("codigo") String codigo) {
        return cursoService.buscarCursoPorCodigo(codigo)
                .map(curso -> Response.ok(new CursoResponseDTO(curso)).build()) // Convert entity to DTO
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({"COORDENADOR"})
    public Response criarCurso(@Valid CursoRequestDTO cursoDto) { // Receives DTO for creation
        try {
            Curso novoCurso = cursoService.criarCurso(cursoDto); // Service now takes DTO
            CursoResponseDTO responseDto = new CursoResponseDTO(novoCurso); // Convert persisted entity to DTO
            return Response.created(UriBuilder.fromResource(CursoResource.class).path(responseDto.id.toString()).build())
                    .entity(responseDto) // Return DTO
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) { // Catch NotFoundException for associated entities (e.g., Disciplinas, Coordenador)
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }


    @PUT
    @Path("/{id}")
    @RolesAllowed({"COORDENADOR"})
    public Response atualizarCurso(@PathParam("id") Long id, @Valid CursoRequestDTO cursoDto) { // Receives DTO for update
        try {
            Curso curso = cursoService.atualizarCurso(id, cursoDto); // Service now takes DTO
            CursoResponseDTO responseDto = new CursoResponseDTO(curso); // Convert updated entity to DTO
            return Response.ok(responseDto).build(); // Return DTO
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"COORDENADOR"}) // ADMIN can also delete courses
    public Response deletarCurso(@PathParam("id") Long id) {
        try {
            boolean deletado = cursoService.deletarCurso(id);
            if (deletado) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}