package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Aluno;
import com.glaydson.controleacademico.rest.dto.AlunoRequestDTO;
import com.glaydson.controleacademico.service.AlunoService;
import jakarta.annotation.security.RolesAllowed; // Para proteção de roles
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.*; // Importa todas as anotações JAX-RS
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder; // Para construir a URI de retorno

import java.util.List;

@Path("/alunos") // Define o caminho base para este recurso
@ApplicationScoped // Torna este recurso um bean gerenciado pelo CDI
@Produces(MediaType.APPLICATION_JSON) // Define o tipo de mídia de resposta padrão como JSON
@Consumes(MediaType.APPLICATION_JSON) // Define o tipo de mídia de requisição padrão como JSON
public class AlunoResource {

    AlunoService alunoService;

    public AlunoResource(AlunoService alunoService) {
        this.alunoService = alunoService; // Construtor para injeção de dependência
    }

    /**
     * Endpoint para listar todos os alunos.
     * Apenas usuários com as roles ADMIN, COORDENADOR, PROFESSOR ou ALUNO podem acessar.
     *
     * @return Uma lista de alunos.
     */
    @GET
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR", "ALUNO"})
    public List<Aluno> listarTodosAlunos() {
        return alunoService.listarTodosAlunos();
    }

    /**
     * Endpoint para buscar um aluno pelo ID.
     * Apenas usuários com as roles ADMIN, COORDENADOR, PROFESSOR ou ALUNO podem acessar.
     *
     * @param id O ID do aluno a ser buscado.
     * @return O aluno encontrado ou 404 Not Found.
     */
    @GET
    @Path("/{id}") // Define um caminho com parâmetro
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR", "ALUNO"})
    public Response buscarAlunoPorId(@PathParam("id") Long id) { // @PathParam para obter o ID do caminho
        return alunoService.buscarAlunoPorId(id)
                .map(aluno -> Response.ok(aluno).build()) // Se encontrou, retorna 200 OK com o aluno
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build()); // Senão, retorna 404 Not Found
    }

    /**
     * Endpoint para buscar um aluno pela matrícula.
     * Apenas usuários com as roles ADMIN, COORDENADOR, PROFESSOR ou ALUNO podem acessar.
     *
     * @param matricula A matrícula do aluno a ser buscado.
     * @return O aluno encontrado ou 404 Not Found.
     */
    @GET
    @Path("/matricula/{matricula}") // Caminho para busca por matrícula
    @RolesAllowed({"ADMIN", "COORDENADOR", "PROFESSOR", "ALUNO"})
    public Response buscarAlunoPorMatricula(@PathParam("matricula") String matricula) {
        return alunoService.buscarAlunoPorMatricula(matricula)
                .map(aluno -> Response.ok(aluno).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * Endpoint para criar um novo aluno.
     * Apenas usuários com a role ADMIN podem criar alunos.
     *
     * @param alunoDto O objeto Aluno a ser criado, enviado no corpo da requisição.
     * @return 201 Created com a localização do novo recurso ou 400 Bad Request se inválido.
     */
    @POST
    @RolesAllowed("ADMIN")
    public Response criarAluno(@Valid AlunoRequestDTO alunoDto) { // <-- Recebe o DTO e valida com @Valid
        try {
            Aluno novoAluno = alunoService.criarAluno(alunoDto); // Passa o DTO para o service
            return Response.created(UriBuilder.fromResource(AlunoResource.class).path(novoAluno.id.toString()).build())
                    .entity(novoAluno)
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    /**
     * Endpoint para atualizar um aluno existente.
     * Apenas usuários com a role ADMIN podem atualizar alunos.
     *
     * @param id O ID do aluno a ser atualizado.
     * @param alunoDto O objeto Aluno com os dados atualizados.
     * @return 200 OK com o aluno atualizado ou 404 Not Found.
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response atualizarAluno(@PathParam("id") Long id, @Valid AlunoRequestDTO alunoDto) { // <-- Recebe o DTO e valida
        try {
            Aluno aluno = alunoService.atualizarAluno(id, alunoDto); // Passa o DTO para o service
            return Response.ok(aluno).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Endpoint para deletar um aluno.
     * Apenas usuários com a role ADMIN podem deletar alunos.
     *
     * @param id O ID do aluno a ser deletado.
     * @return 204 No Content se deletado com sucesso, ou 404 Not Found.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response deletarAluno(@PathParam("id") Long id) {
        boolean deletado = alunoService.deletarAluno(id);
        if (deletado) {
            return Response.noContent().build(); // Retorna 204 No Content para sucesso de deleção
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}