package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.service.CursoService;
import com.glaydson.controleacademico.rest.dto.CursoRequestDTO;
import com.glaydson.controleacademico.rest.dto.CursoResponseDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CursoResourceTest {

    @Inject
    CursoService cursoService;

    @Inject
    CursoResource cursoResource;

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testListarTodosCursos() {
        // Clean up and seed the database
        CursoRequestDTO curso1 = new CursoRequestDTO();
        curso1.nome = "Curso 1";
        curso1.codigo = "C1";
        CursoRequestDTO curso2 = new CursoRequestDTO();
        curso2.nome = "Curso 2";
        curso2.codigo = "C2";
        cursoResource.criarCurso(curso1);
        cursoResource.criarCurso(curso2);

        List<CursoResponseDTO> result = cursoResource.listarTodosCursos();
        assertNotNull(result);
        assertTrue(result.size() >= 2); // There may be more if DB is not empty
        boolean foundCurso1 = result.stream().anyMatch(c -> "Curso 1".equals(c.nome) && "C1".equals(c.codigo));
        boolean foundCurso2 = result.stream().anyMatch(c -> "Curso 2".equals(c.nome) && "C2".equals(c.codigo));
        assertTrue(foundCurso1, "Curso 1 should be present in the result");
        assertTrue(foundCurso2, "Curso 2 should be present in the result");
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarCurso() {
        CursoRequestDTO requestDTO = new CursoRequestDTO();
        requestDTO.nome = "Integration Test Course";
        requestDTO.codigo = "ITC101";
        // ...set other properties if required...

        Response response = cursoResource.criarCurso(requestDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        CursoResponseDTO responseBody = (CursoResponseDTO) response.getEntity();
        assertNotNull(responseBody);
        assertEquals("Integration Test Course", responseBody.nome);
        assertEquals("ITC101", responseBody.codigo);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarCursoJaExistente() {
        CursoRequestDTO requestDTO = new CursoRequestDTO();
        requestDTO.nome = "Curso Duplicado";
        requestDTO.codigo = "CDUP";
        // Cria o curso pela primeira vez
        Response response1 = cursoResource.criarCurso(requestDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());

        // Tenta criar o mesmo curso novamente
        Response response2 = cursoResource.criarCurso(requestDTO);
        // Espera-se que retorne erro de conflito ou bad request
        assertTrue(response2.getStatus() == Response.Status.CONFLICT.getStatusCode() ||
                   response2.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testRemoverCurso() {
        // Cria um curso para remover
        CursoRequestDTO requestDTO = new CursoRequestDTO();
        requestDTO.nome = "Curso Para Remover";
        requestDTO.codigo = "CPR";
        Response response = cursoResource.criarCurso(requestDTO);
        CursoResponseDTO created = (CursoResponseDTO) response.getEntity();
        assertNotNull(created);
        Long id = created.id;
        // Remove o curso
        Response deleteResponse = cursoResource.deletarCurso(id);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testRemoverCursoNaoExistente() {
        // Tenta remover um curso com ID improvável
        Long nonExistentId = 999999L;
        Response response = cursoResource.deletarCurso(nonExistentId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testAtualizarCursoComCodigoExistente() {
        // Cria dois cursos
        CursoRequestDTO curso1 = new CursoRequestDTO();
        curso1.nome = "Curso Original";
        curso1.codigo = "COD1";
        CursoResponseDTO created1 = (CursoResponseDTO) cursoResource.criarCurso(curso1).getEntity();

        CursoRequestDTO curso2 = new CursoRequestDTO();
        curso2.nome = "Outro Curso";
        curso2.codigo = "COD2";
        CursoResponseDTO created2 = (CursoResponseDTO) cursoResource.criarCurso(curso2).getEntity();

        // Tenta atualizar o segundo curso para ter o mesmo código do primeiro
        CursoRequestDTO updateDTO = new CursoRequestDTO();
        updateDTO.nome = "Outro Curso";
        updateDTO.codigo = "COD1"; // Código já existente
        Response response = cursoResource.atualizarCurso(created2.id, updateDTO);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testAtualizarCursoComNomeExistente() {
        // Cria dois cursos
        CursoRequestDTO curso1 = new CursoRequestDTO();
        curso1.nome = "Nome Unico";
        curso1.codigo = "CODA";
        CursoResponseDTO created1 = (CursoResponseDTO) cursoResource.criarCurso(curso1).getEntity();

        CursoRequestDTO curso2 = new CursoRequestDTO();
        curso2.nome = "Nome Diferente";
        curso2.codigo = "CODB";
        CursoResponseDTO created2 = (CursoResponseDTO) cursoResource.criarCurso(curso2).getEntity();

        // Tenta atualizar o segundo curso para ter o mesmo nome do primeiro
        CursoRequestDTO updateDTO = new CursoRequestDTO();
        updateDTO.nome = "Nome Unico"; // Nome já existente
        updateDTO.codigo = "CODB";
        Response response = cursoResource.atualizarCurso(created2.id, updateDTO);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
}
