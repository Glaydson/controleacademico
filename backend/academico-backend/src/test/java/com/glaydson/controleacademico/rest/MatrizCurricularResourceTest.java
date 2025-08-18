package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.rest.dto.MatrizCurricularRequestDTO;
import com.glaydson.controleacademico.rest.dto.PeriodoMatrizRequestDTO;
import com.glaydson.controleacademico.rest.dto.MatrizCurricularResponseDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatrizCurricularResourceTest {

    @Inject
    MatrizCurricularResource matrizCurricularResource;

    private Long cursoId;
    private Long disciplinaId1;
    private Long disciplinaId2;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up
        com.glaydson.controleacademico.domain.model.PeriodoMatriz.deleteAll();
        com.glaydson.controleacademico.domain.model.MatrizCurricular.deleteAll();
        Disciplina.deleteAll();
        Curso.deleteAll();
        // Seed Curso
        Curso curso = new Curso("Engenharia de Computação", "EC001");
        curso.persist();
        cursoId = curso.id;
        // Seed Disciplinas
        Disciplina d1 = new Disciplina("Matemática", "MAT01", curso);
        d1.persist();
        disciplinaId1 = d1.id;
        Disciplina d2 = new Disciplina("Algoritmos", "ALG01", curso);
        d2.persist();
        disciplinaId2 = d2.id;
    }

    @Test
    @Order(1)
    @TestSecurity(user = "coord", roles = {"COORDENADOR"})
    void testCriarMatrizCurricularComPeriodos() {
        PeriodoMatrizRequestDTO periodo1 = new PeriodoMatrizRequestDTO(1, new HashSet<>(Arrays.asList(disciplinaId1)));
        PeriodoMatrizRequestDTO periodo2 = new PeriodoMatrizRequestDTO(2, new HashSet<>(Arrays.asList(disciplinaId2)));
        MatrizCurricularRequestDTO request = new MatrizCurricularRequestDTO(cursoId, Arrays.asList(periodo1, periodo2));
        var response = matrizCurricularResource.criarMatrizCurricular(request);
        assertEquals(201, response.getStatus());
        MatrizCurricularResponseDTO dto = (MatrizCurricularResponseDTO) response.getEntity();
        assertNotNull(dto);
        assertEquals(cursoId, dto.cursoId);
        assertEquals(2, dto.periodos.size());
    }

    @Test
    @Order(2)
    @TestSecurity(user = "coord", roles = {"COORDENADOR"})
    void testListarTodasMatrizesCurriculares() {
        testCriarMatrizCurricularComPeriodos();
        List<MatrizCurricularResponseDTO> result = matrizCurricularResource.listarTodasMatrizesCurriculares();
        assertNotNull(result);
        assertTrue(result.size() >= 1);
    }

    @Test
    @Order(3)
    @TestSecurity(user = "coord", roles = {"COORDENADOR"})
    void testBuscarMatrizCurricularPorId() {
        var createResp = matrizCurricularResource.criarMatrizCurricular(new MatrizCurricularRequestDTO(cursoId, Arrays.asList(new PeriodoMatrizRequestDTO(1, new HashSet<>(Arrays.asList(disciplinaId1))))));
        MatrizCurricularResponseDTO created = (MatrizCurricularResponseDTO) createResp.getEntity();
        var getResp = matrizCurricularResource.buscarMatrizCurricularPorId(created.id);
        assertEquals(200, getResp.getStatus());
        MatrizCurricularResponseDTO dto = (MatrizCurricularResponseDTO) getResp.getEntity();
        assertEquals(created.id, dto.id);
        assertEquals(cursoId, dto.cursoId);
    }

    @Test
    @Order(4)
    @TestSecurity(user = "coord", roles = {"COORDENADOR"})
    void testAtualizarMatrizCurricular() {
        var createResp = matrizCurricularResource.criarMatrizCurricular(new MatrizCurricularRequestDTO(cursoId, Arrays.asList(new PeriodoMatrizRequestDTO(1, new HashSet<>(Arrays.asList(disciplinaId1))))));
        MatrizCurricularResponseDTO created = (MatrizCurricularResponseDTO) createResp.getEntity();
        // Update: add a second period
        PeriodoMatrizRequestDTO periodo2 = new PeriodoMatrizRequestDTO(2, new HashSet<>(Arrays.asList(disciplinaId2)));
        MatrizCurricularRequestDTO updateRequest = new MatrizCurricularRequestDTO(cursoId, Arrays.asList(new PeriodoMatrizRequestDTO(1, new HashSet<>(Arrays.asList(disciplinaId1))), periodo2));
        var updateResp = matrizCurricularResource.atualizarMatrizCurricular(created.id, updateRequest);
        assertEquals(200, updateResp.getStatus());
        MatrizCurricularResponseDTO updated = (MatrizCurricularResponseDTO) updateResp.getEntity();
        assertEquals(2, updated.periodos.size());
    }

    @Test
    @Order(5)
    @TestSecurity(user = "coord", roles = {"COORDENADOR"})
    void testDeletarMatrizCurricular() {
        var createResp = matrizCurricularResource.criarMatrizCurricular(new MatrizCurricularRequestDTO(cursoId, Arrays.asList(new PeriodoMatrizRequestDTO(1, new HashSet<>(Arrays.asList(disciplinaId1))))));
        MatrizCurricularResponseDTO created = (MatrizCurricularResponseDTO) createResp.getEntity();
        var deleteResp = matrizCurricularResource.deletarMatrizCurricular(created.id);
        assertEquals(204, deleteResp.getStatus());
    }

    @Test
    @Order(6)
    @TestSecurity(user = "coord", roles = {"COORDENADOR"})
    void testValidacaoCursoObrigatorio() {
        MatrizCurricularRequestDTO request = new MatrizCurricularRequestDTO(null, Arrays.asList(new PeriodoMatrizRequestDTO(1, new HashSet<>(Arrays.asList(disciplinaId1)))));
        assertThrows(Exception.class, () -> matrizCurricularResource.criarMatrizCurricular(request));
    }

    @Test
    @Order(7)
    @TestSecurity(user = "coord", roles = {"COORDENADOR"})
    void testValidacaoDisciplinaInexistente() {
        MatrizCurricularRequestDTO request = new MatrizCurricularRequestDTO(cursoId, Arrays.asList(new PeriodoMatrizRequestDTO(1, new HashSet<>(Arrays.asList(99999L)))));
        assertThrows(Exception.class, () -> matrizCurricularResource.criarMatrizCurricular(request));
    }

    @Test
    @Order(8)
    @TestSecurity(user = "prof", roles = {"PROFESSOR"})
    void testAcessoNegadoParaProfessorCriar() {
        MatrizCurricularRequestDTO request = new MatrizCurricularRequestDTO(cursoId, Arrays.asList(new PeriodoMatrizRequestDTO(1, new HashSet<>(Arrays.asList(disciplinaId1)))));
        assertThrows(io.quarkus.security.ForbiddenException.class, () -> matrizCurricularResource.criarMatrizCurricular(request));
    }
}
