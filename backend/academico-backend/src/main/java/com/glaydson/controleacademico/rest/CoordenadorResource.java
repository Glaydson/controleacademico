package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Coordenador;
import com.glaydson.controleacademico.rest.dto.CoordenadorRequestDTO;
import com.glaydson.controleacademico.rest.dto.CoordenadorResponseDTO;
import com.glaydson.controleacademico.service.CoordenadorService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;
import java.util.stream.Collectors;

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
    @RolesAllowed({"ADMIN"})
    public List<CoordenadorResponseDTO> listarTodosCoordenadores() {
        return coordenadorService.listarTodosCoordenadores().stream()
                .map(CoordenadorResponseDTO::new) // Converte entidade para DTO
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN"})
    public Response buscarCoordenadorPorId(@PathParam("id") Long id) {
        return coordenadorService.buscarCoordenadorPorId(id)
                .map(coordenador -> Response.ok(new CoordenadorResponseDTO(coordenador)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/matricula/{matricula}")
    @RolesAllowed({"ADMIN"})
    public Response buscarCoordenadorPorMatricula(@PathParam("matricula") String matricula) {
        return coordenadorService.buscarCoordenadorPorMatricula(matricula)
                .map(coordenador -> Response.ok(new CoordenadorResponseDTO(coordenador)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response criarCoordenador(@Valid CoordenadorRequestDTO coordenadorDto) {
        try {
            Coordenador novoCoordenador = coordenadorService.criarCoordenador(coordenadorDto);
            CoordenadorResponseDTO responseDto = new CoordenadorResponseDTO(novoCoordenador);
            return Response.created(UriBuilder.fromResource(CoordenadorResource.class).path(responseDto.id.toString()).build())
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
    @RolesAllowed("ADMIN")
    public Response atualizarCoordenador(@PathParam("id") Long id, @Valid CoordenadorRequestDTO coordenadorDto) {
        try {
            Coordenador coordenador = coordenadorService.atualizarCoordenador(id, coordenadorDto);
            CoordenadorResponseDTO responseDto = new CoordenadorResponseDTO(coordenador);
            return Response.ok(responseDto).build();
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