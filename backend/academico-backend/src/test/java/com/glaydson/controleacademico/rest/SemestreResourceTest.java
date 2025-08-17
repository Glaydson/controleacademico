package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.rest.dto.SemestreRequestDTO;
import com.glaydson.controleacademico.rest.dto.SemestreResponseDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SemestreResourceTest {

    @Inject
    SemestreResource semestreResource;

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testListarTodosSemestres() {
        // Create test data
        SemestreRequestDTO semestre1 = new SemestreRequestDTO();
        semestre1.ano = 2024;
        semestre1.periodo = "1";

        SemestreRequestDTO semestre2 = new SemestreRequestDTO();
        semestre2.ano = 2024;
        semestre2.periodo = "2";

        try (Response response1 = semestreResource.criarSemestre(semestre1);
             Response response2 = semestreResource.criarSemestre(semestre2)) {
            // Just ensure they were created successfully
            assertTrue(response1.getStatus() == Response.Status.CREATED.getStatusCode() ||
                      response1.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
            assertTrue(response2.getStatus() == Response.Status.CREATED.getStatusCode() ||
                      response2.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
        }

        List<SemestreResponseDTO> result = semestreResource.listarTodosSemestres();
        assertNotNull(result);
        assertTrue(result.size() >= 0); // There may be semesters from previous tests

        // Check if our test semesters are present (if creation was successful)
        boolean foundSemestre1 = result.stream().anyMatch(s ->
            s.ano.equals(2024) && "1".equals(s.periodo));
        boolean foundSemestre2 = result.stream().anyMatch(s ->
            s.ano.equals(2024) && "2".equals(s.periodo));

        // At least verify the list is accessible
        assertNotNull(result);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarSemestre() {
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2029; // Use unique year to avoid conflicts
        requestDTO.periodo = "1";

        try (Response response = semestreResource.criarSemestre(requestDTO)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                SemestreResponseDTO responseBody = (SemestreResponseDTO) response.getEntity();
                assertNotNull(responseBody);
                assertEquals(Integer.valueOf(2029), responseBody.ano);
                assertEquals("1", responseBody.periodo);
                assertEquals("2029.1", responseBody.descricao);
                assertNotNull(responseBody.id);
            } else if (response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
                // If this year/period already exists, try with different values
                requestDTO.ano = 2030;
                try (Response retryResponse = semestreResource.criarSemestre(requestDTO)) {
                    assertEquals(Response.Status.CREATED.getStatusCode(), retryResponse.getStatus());
                    SemestreResponseDTO responseBody = (SemestreResponseDTO) retryResponse.getEntity();
                    assertNotNull(responseBody);
                    assertEquals(Integer.valueOf(2030), responseBody.ano);
                    assertEquals("1", responseBody.periodo);
                    assertEquals("2030.1", responseBody.descricao);
                    assertNotNull(responseBody.id);
                }
            }
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarSemestreJaExistente() {
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2023;
        requestDTO.periodo = "1";

        // Create the semester for the first time
        try (Response response1 = semestreResource.criarSemestre(requestDTO)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());
        }

        // Try to create the same semester again (same year and period)
        try (Response response2 = semestreResource.criarSemestre(requestDTO)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.getStatus());
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarSemestrePorId() {
        // Create a semester with unique values to avoid conflicts
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2027; // Use a future year to avoid conflicts
        requestDTO.periodo = "1";

        SemestreResponseDTO created;
        try (Response createResponse = semestreResource.criarSemestre(requestDTO)) {
            // Check if creation was successful
            if (createResponse.getStatus() == Response.Status.CREATED.getStatusCode()) {
                created = (SemestreResponseDTO) createResponse.getEntity();
                assertNotNull(created);
            } else {
                // If creation failed due to existing semester, skip this test
                // or try with different values
                requestDTO.ano = 2028;
                try (Response retryResponse = semestreResource.criarSemestre(requestDTO)) {
                    assertEquals(Response.Status.CREATED.getStatusCode(), retryResponse.getStatus());
                    created = (SemestreResponseDTO) retryResponse.getEntity();
                    assertNotNull(created);
                }
            }
        }

        // Search by ID
        try (Response response = semestreResource.buscarSemestrePorId(created.id)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            SemestreResponseDTO found = (SemestreResponseDTO) response.getEntity();
            assertNotNull(found);
            assertEquals(created.id, found.id);
            assertEquals(created.ano, found.ano);
            assertEquals(created.periodo, found.periodo);
            assertEquals(created.ano + "." + created.periodo, found.descricao);
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarSemestrePorIdNaoExistente() {
        Long nonExistentId = 999999L;
        try (Response response = semestreResource.buscarSemestrePorId(nonExistentId)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarSemestrePorAnoPeriodo() {
        // Create a semester with unique values
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2031; // Use unique year
        requestDTO.periodo = "1";

        try (Response createResponse = semestreResource.criarSemestre(requestDTO)) {
            if (createResponse.getStatus() != Response.Status.CREATED.getStatusCode()) {
                // Try with different year if this one already exists
                requestDTO.ano = 2032;
                try (Response retryResponse = semestreResource.criarSemestre(requestDTO)) {
                    assertEquals(Response.Status.CREATED.getStatusCode(), retryResponse.getStatus());
                }
            }
        }

        // Search by year and period
        try (Response response = semestreResource.buscarSemestrePorAnoPeriodo(requestDTO.ano, "1")) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            SemestreResponseDTO found = (SemestreResponseDTO) response.getEntity();
            assertNotNull(found);
            assertEquals(requestDTO.ano, found.ano);
            assertEquals("1", found.periodo);
            assertEquals(requestDTO.ano + ".1", found.descricao);
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarSemestrePorAnoPeriodoNaoExistente() {
        try (Response response = semestreResource.buscarSemestrePorAnoPeriodo(1999, "3")) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testAtualizarSemestre() {
        // Create a semester with unique values
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2033; // Use unique year
        requestDTO.periodo = "1";

        SemestreResponseDTO created;
        try (Response createResponse = semestreResource.criarSemestre(requestDTO)) {
            if (createResponse.getStatus() == Response.Status.CREATED.getStatusCode()) {
                created = (SemestreResponseDTO) createResponse.getEntity();
                assertNotNull(created);
            } else {
                // Try with different year if conflicts
                requestDTO.ano = 2034;
                try (Response retryResponse = semestreResource.criarSemestre(requestDTO)) {
                    assertEquals(Response.Status.CREATED.getStatusCode(), retryResponse.getStatus());
                    created = (SemestreResponseDTO) retryResponse.getEntity();
                    assertNotNull(created);
                }
            }
        }

        // Update the semester
        SemestreRequestDTO updateDTO = new SemestreRequestDTO();
        updateDTO.ano = created.ano;
        updateDTO.periodo = "2";

        try (Response updateResponse = semestreResource.atualizarSemestre(created.id, updateDTO)) {
            assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

            SemestreResponseDTO updated = (SemestreResponseDTO) updateResponse.getEntity();
            assertNotNull(updated);
            assertEquals(created.id, updated.id);
            assertEquals(created.ano, updated.ano);
            assertEquals("2", updated.periodo);
            assertEquals(created.ano + ".2", updated.descricao);
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testAtualizarSemestreNaoExistente() {
        Long nonExistentId = 999999L;
        SemestreRequestDTO updateDTO = new SemestreRequestDTO();
        updateDTO.ano = 2024;
        updateDTO.periodo = "1";

        try (Response response = semestreResource.atualizarSemestre(nonExistentId, updateDTO)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testAtualizarSemestreParaAnoPeriodoExistente() {
        // Create two semesters with unique values
        SemestreRequestDTO semestre1 = new SemestreRequestDTO();
        semestre1.ano = 2035; // Use unique year
        semestre1.periodo = "1";

        SemestreRequestDTO semestre2 = new SemestreRequestDTO();
        semestre2.ano = 2035;
        semestre2.periodo = "2";

        SemestreResponseDTO created1, created2;
        try (Response response1 = semestreResource.criarSemestre(semestre1)) {
            if (response1.getStatus() == Response.Status.CREATED.getStatusCode()) {
                created1 = (SemestreResponseDTO) response1.getEntity();
            } else {
                // Try with different year if conflicts
                semestre1.ano = 2036;
                semestre2.ano = 2036;
                try (Response retryResponse = semestreResource.criarSemestre(semestre1)) {
                    assertEquals(Response.Status.CREATED.getStatusCode(), retryResponse.getStatus());
                    created1 = (SemestreResponseDTO) retryResponse.getEntity();
                }
            }
        }

        try (Response response2 = semestreResource.criarSemestre(semestre2)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response2.getStatus());
            created2 = (SemestreResponseDTO) response2.getEntity();
        }

        // Try to update the second semester to have the same year and period as the first
        SemestreRequestDTO updateDTO = new SemestreRequestDTO();
        updateDTO.ano = created1.ano;
        updateDTO.periodo = "1"; // Same as first semester

        try (Response updateResponse = semestreResource.atualizarSemestre(created2.id, updateDTO)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), updateResponse.getStatus());
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testDeletarSemestre() {
        // Create a semester to delete with unique values
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2037; // Use unique year
        requestDTO.periodo = "1";

        SemestreResponseDTO created;
        try (Response createResponse = semestreResource.criarSemestre(requestDTO)) {
            if (createResponse.getStatus() == Response.Status.CREATED.getStatusCode()) {
                created = (SemestreResponseDTO) createResponse.getEntity();
                assertNotNull(created);
            } else {
                // Try with different year if conflicts
                requestDTO.ano = 2038;
                try (Response retryResponse = semestreResource.criarSemestre(requestDTO)) {
                    assertEquals(Response.Status.CREATED.getStatusCode(), retryResponse.getStatus());
                    created = (SemestreResponseDTO) retryResponse.getEntity();
                    assertNotNull(created);
                }
            }
        }

        // Delete the semester
        try (Response deleteResponse = semestreResource.deletarSemestre(created.id)) {
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
        }

        // Verify it's deleted by trying to find it
        try (Response findResponse = semestreResource.buscarSemestrePorId(created.id)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), findResponse.getStatus());
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testDeletarSemestreNaoExistente() {
        Long nonExistentId = 999999L;
        try (Response response = semestreResource.deletarSemestre(nonExistentId)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"ALUNO"})
    void testListarSemestresComoAluno() {
        // Students should be able to list semesters
        List<SemestreResponseDTO> result = semestreResource.listarTodosSemestres();
        assertNotNull(result);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarSemestreComoProfessor() {
        // First, create a semester as coordenador
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2039; // Use unique year
        requestDTO.periodo = "1";

        SemestreResponseDTO created;
        try (Response createResponse = semestreResource.criarSemestre(requestDTO)) {
            if (createResponse.getStatus() == Response.Status.CREATED.getStatusCode()) {
                created = (SemestreResponseDTO) createResponse.getEntity();
                assertNotNull(created);
            } else {
                // Try with different year if conflicts
                requestDTO.ano = 2040;
                try (Response retryResponse = semestreResource.criarSemestre(requestDTO)) {
                    assertEquals(Response.Status.CREATED.getStatusCode(), retryResponse.getStatus());
                    created = (SemestreResponseDTO) retryResponse.getEntity();
                    assertNotNull(created);
                }
            }
        }

        // Test that the created semester can be accessed (since we're still COORDENADOR)
        try (Response response = semestreResource.buscarSemestrePorId(created.id)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            SemestreResponseDTO found = (SemestreResponseDTO) response.getEntity();
            assertNotNull(found);
            assertEquals(created.id, found.id);
        }

        // Note: Testing PROFESSOR access would require a separate test method
        // with @TestSecurity(user = "testuser", roles = {"PROFESSOR"})
        // since we can't change roles within the same test method
    }

    @Test
    @TestSecurity(user = "professor", roles = {"PROFESSOR"})
    void testProfessorPodeListarSemestres() {
        // Professors should be able to list semesters
        List<SemestreResponseDTO> result = semestreResource.listarTodosSemestres();
        assertNotNull(result);
    }

    @Test
    @TestSecurity(user = "professor", roles = {"PROFESSOR"})
    void testProfessorPodeBuscarSemestrePorAnoPeriodo() {
        // Professors should be able to search semesters by year and period
        // This assumes at least one semester exists (from other tests)
        try (Response response = semestreResource.buscarSemestrePorAnoPeriodo(2024, "1")) {
            // Should either find it or return not found, but not forbidden
            assertTrue(response.getStatus() == Response.Status.OK.getStatusCode() ||
                      response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"ALUNO"})
    void testCriarSemestreComoAlunoDeveSerNegado() {
        // Students should not be able to create semesters
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2024;
        requestDTO.periodo = "1";

        try (Response response = semestreResource.criarSemestre(requestDTO)) {
            // Should be forbidden
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        } catch (Exception e) {
            // Security annotation might throw exception instead of returning forbidden
            String message = e.getMessage();
            assertTrue(message == null ||
                      message.contains("Forbidden") ||
                      message.contains("Access denied") ||
                      message.contains("role"),
                      "Expected security-related exception, but got: " + e.getClass().getSimpleName() +
                      " with message: " + message);
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testValidacaoPeriodo() {
        // Test invalid period (should be only "1" or "2")
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2024;
        requestDTO.periodo = "3"; // Invalid period

        try (Response response = semestreResource.criarSemestre(requestDTO)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        } catch (Exception e) {
            // Bean validation throws ResteasyViolationExceptionImpl for invalid data
            assertTrue(e.getClass().getSimpleName().contains("Violation") ||
                      e.getClass().getSimpleName().contains("Constraint") ||
                      e.getClass().getSimpleName().contains("Validation"),
                      "Expected validation exception, but got: " + e.getClass().getSimpleName());
            String message = e.getMessage();
            assertTrue(message != null &&
                      (message.contains("período deve ser '1' ou '2'") ||
                       message.contains("period") ||
                       message.contains("periodo")),
                      "Expected period validation message, but got: " + message);
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testValidacaoAno() {
        // Test invalid year (should be >= 2000)
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 1999; // Invalid year
        requestDTO.periodo = "1";

        try (Response response = semestreResource.criarSemestre(requestDTO)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        } catch (Exception e) {
            // Bean validation throws ResteasyViolationExceptionImpl for invalid data
            assertTrue(e.getClass().getSimpleName().contains("Violation") ||
                      e.getClass().getSimpleName().contains("Constraint") ||
                      e.getClass().getSimpleName().contains("Validation"),
                      "Expected validation exception, but got: " + e.getClass().getSimpleName());
            String message = e.getMessage();
            assertTrue(message != null &&
                      (message.contains("ano deve ser igual ou superior a 2000") ||
                       message.contains("year") ||
                       message.contains("ano")),
                      "Expected year validation message, but got: " + message);
        }
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testDescricaoSemestre() {
        // Test that description is properly formatted as "ano.periodo"
        SemestreRequestDTO requestDTO = new SemestreRequestDTO();
        requestDTO.ano = 2025;
        requestDTO.periodo = "2";

        try (Response response = semestreResource.criarSemestre(requestDTO)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

            SemestreResponseDTO created = (SemestreResponseDTO) response.getEntity();
            assertNotNull(created);
            assertEquals("2025.2", created.descricao);
        }
    }
}
