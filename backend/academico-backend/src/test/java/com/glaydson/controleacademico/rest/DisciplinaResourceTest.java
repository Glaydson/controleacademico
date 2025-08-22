package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.model.Professor;
import com.glaydson.controleacademico.domain.repository.ProfessorRepository;
import com.glaydson.controleacademico.rest.dto.DisciplinaRequestDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DisciplinaResourceTest {
    private Long cursoId;
    private Long segundoCursoId;
    private Long professorId;
    @Inject
    ProfessorRepository professorRepository;
    @BeforeEach
    @Transactional
    void setUp() {
        Disciplina.deleteAll();
        Curso.deleteAll();
        Professor.deleteAll();
        Curso curso1 = new Curso("Ciência da Computação", "CC001");
        curso1.persist();
        cursoId = curso1.id;
        Curso curso2 = new Curso("Engenharia de Software", "ES001");
        curso2.persist();
        segundoCursoId = curso2.id;
        Professor professor = new Professor("Prof. Teste", "REG123", null, "keycloak-prof-1");
        professor.persist();
        professorId = professor.id;
    }
    @Test
    @Order(1)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarDisciplinaComCursoAssociado() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Algoritmos e Estruturas de Dados",
            "AED001",
            cursoId,
            null
        );
        given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201)
            .body("nome", equalTo("Algoritmos e Estruturas de Dados"))
            .body("codigo", equalTo("AED001"))
            .body("curso.id", equalTo(cursoId.intValue()))
            .body("curso.nome", equalTo("Ciência da Computação"))
            .body("curso.codigo", equalTo("CC001"));
    }
    @Test
    @Order(2)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarDisciplinaSemCurso() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO();
        disciplinaDto.nome = "Matemática Discreta";
        disciplinaDto.codigo = "MAT001";
        // cursoId é null (obrigatório)

        given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(400); // Bad Request devido à validação @NotNull
    }
    @Test
    @Order(3)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarDisciplinaComCursoInexistente() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Programação Orientada a Objetos",
            "POO001",
            999L,
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(404) // Not Found
            .body(containsString("Curso com ID 999 não encontrado"));
    }
    @Test
    @Order(4)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarDisciplinaComCodigoRepetido() {
        // Primeiro, criar uma disciplina
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Banco de Dados I",
            "BD001",
            cursoId,
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(disciplina1)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201);

        // Tentar criar outra disciplina com o mesmo código
        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Banco de Dados II",
            "BD001", // Mesmo código
            segundoCursoId,
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(disciplina2)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(400) // Bad Request
            .body(containsString("Já existe uma disciplina com o código BD001"));
    }
    @Test
    @Order(5)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarDisciplinaComNomeRepetido() {
        // Primeiro, criar uma disciplina
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Inteligência Artificial",
            "IA001",
            cursoId,
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(disciplina1)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201);

        // Tentar criar outra disciplina com o mesmo nome
        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Inteligência Artificial", // Mesmo nome
            "IA002",
            segundoCursoId,
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(disciplina2)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(400) // Bad Request
            .body(containsString("Já existe uma disciplina com o nome Inteligência Artificial"));
    }
    @Test
    @Order(6)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarDisciplinaPorId() {
        // Criar disciplina para teste
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Sistemas Operacionais",
            "SO001",
            cursoId,
            null
        );

        Integer extractedId = given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        Long id = extractedId.longValue();

        // Buscar por ID
        given()
            .when()
            .get("/disciplinas/" + id)
            .then()
            .statusCode(200)
            .body("id", equalTo(id.intValue()))
            .body("nome", equalTo("Sistemas Operacionais"))
            .body("codigo", equalTo("SO001"))
            .body("curso.id", equalTo(cursoId.intValue()));
    }
    @Test
    @Order(7)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarDisciplinaPorIdInexistente() {
        given()
            .when()
            .get("/disciplinas/999")
            .then()
            .statusCode(404); // Not Found
    }
    @Test
    @Order(8)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarDisciplinaPorCodigo() {
        // Criar disciplina para teste (requires COORDENADOR)
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Redes de Computadores",
            "RC001",
            cursoId,
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201);

        // Buscar por código (can be done with any role, but using COORDENADOR for consistency)
        given()
            .when()
            .get("/disciplinas/codigo/RC001")
            .then()
            .statusCode(200)
            .body("nome", equalTo("Redes de Computadores"))
            .body("codigo", equalTo("RC001"))
            .body("curso.id", equalTo(cursoId.intValue()));
    }
    @Test
    @Order(9)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testBuscarDisciplinaPorCodigoInexistente() {
        given()
            .when()
            .get("/disciplinas/codigo/INEXISTENTE")
            .then()
            .statusCode(404); // Not Found
    }
    @Test
    @Order(10)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testListarTodasDisciplinas() {
        // Criar algumas disciplinas para teste (requires COORDENADOR)
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Compiladores",
            "COMP001",
            cursoId,
            null
        );

        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Engenharia de Software",
            "ENGS001",
            segundoCursoId,
            null
        );

        // Criar as disciplinas
        given()
            .contentType(ContentType.JSON)
            .body(disciplina1)
            .post("/disciplinas")
            .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(disciplina2)
            .post("/disciplinas")
            .then()
            .statusCode(201);

        // Listar todas as disciplinas (can be done with any role, but using COORDENADOR for consistency)
        given()
            .when()
            .get("/disciplinas")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("find { it.codigo == 'COMP001' }.nome", equalTo("Compiladores"))
            .body("find { it.codigo == 'ENGS001' }.nome", equalTo("Engenharia de Software"));
    }
    @Test
    @Order(11)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testAtualizarDisciplina() {
        // Criar disciplina para teste
        DisciplinaRequestDTO disciplinaOriginal = new DisciplinaRequestDTO(
            "Programação Web",
            "WEB001",
            cursoId,
            null
        );

        Integer extractedId = given()
            .contentType(ContentType.JSON)
            .body(disciplinaOriginal)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        Long id = extractedId.longValue();

        // Atualizar a disciplina (incluindo mudança de curso)
        DisciplinaRequestDTO disciplinaAtualizada = new DisciplinaRequestDTO(
            "Desenvolvimento Web Avançado",
            "WEB002",
            segundoCursoId,
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(disciplinaAtualizada)
            .when()
            .put("/disciplinas/" + id)
            .then()
            .statusCode(200)
            .body("id", equalTo(id.intValue()))
            .body("nome", equalTo("Desenvolvimento Web Avançado"))
            .body("codigo", equalTo("WEB002"))
            .body("curso.id", equalTo(segundoCursoId.intValue()))
            .body("curso.nome", equalTo("Engenharia de Software"));
    }
    @Test
    @Order(12)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testAtualizarDisciplinaInexistente() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Disciplina Inexistente",
            "INX001",
            cursoId,
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .put("/disciplinas/999")
            .then()
            .statusCode(404); // Not Found
    }
    @Test
    @Order(13)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testDeletarDisciplina() {
        // Criar disciplina para teste
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Segurança da Informação",
            "SEG001",
            cursoId,
            null
        );

        Integer extractedId = given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        Long id = extractedId.longValue();

        // Deletar a disciplina
        given()
            .when()
            .delete("/disciplinas/" + id)
            .then()
            .statusCode(204); // No Content

        // Verificar se foi deletada
        given()
            .when()
            .get("/disciplinas/" + id)
            .then()
            .statusCode(404); // Not Found
    }
    @Test
    @Order(14)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testDeletarDisciplinaInexistente() {
        given()
            .when()
            .delete("/disciplinas/999")
            .then()
            .statusCode(404); // Not Found
    }
    @Test
    @Order(15)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testValidacaoCamposObrigatorios() {
        // Testar nome em branco
        DisciplinaRequestDTO disciplinaSemNome = new DisciplinaRequestDTO();
        disciplinaSemNome.nome = "";
        disciplinaSemNome.codigo = "TEST001";
        disciplinaSemNome.cursoId = cursoId;

        given()
            .contentType(ContentType.JSON)
            .body(disciplinaSemNome)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(400); // Bad Request due to validation

        // Testar código em branco
        DisciplinaRequestDTO disciplinaSemCodigo = new DisciplinaRequestDTO();
        disciplinaSemCodigo.nome = "Disciplina Teste";
        disciplinaSemCodigo.codigo = "";
        disciplinaSemCodigo.cursoId = cursoId;

        given()
            .contentType(ContentType.JSON)
            .body(disciplinaSemCodigo)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(400); // Bad Request due to validation
    }
    @Test
    @Order(16)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarDisciplinaComProfessor() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Machine Learning",
            "ML001",
            cursoId,
            professorId
        );
        Integer extractedId = given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201)
            .body("nome", equalTo("Machine Learning"))
            .body("codigo", equalTo("ML001"))
            .body("curso.id", equalTo(cursoId.intValue()))
            .body("professor.id", equalTo(professorId.intValue()))
            .body("professor.nome", equalTo("Prof. Teste"))
            .extract()
            .path("id");
    }
    @Test
    @Order(17)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testCriarDisciplinaSemProfessor() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Deep Learning",
            "DL001",
            cursoId,
            null
        );
        Integer extractedId = given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201)
            .body("nome", equalTo("Deep Learning"))
            .body("codigo", equalTo("DL001"))
            .body("curso.id", equalTo(cursoId.intValue()))
            .body("professor", nullValue())
            .extract()
            .path("id");
    }
    @Test
    @Order(18)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testAtualizarDisciplinaProfessor() {
        // Criar disciplina sem professor
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "IA Avançada",
            "IAA001",
            cursoId,
            null
        );
        Integer extractedId = given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201)
            .extract()
            .path("id");
        Long id = extractedId.longValue();
        // Atualizar para adicionar professor
        DisciplinaRequestDTO atualizada = new DisciplinaRequestDTO(
            "IA Avançada",
            "IAA001",
            cursoId,
            professorId
        );
        given()
            .contentType(ContentType.JSON)
            .body(atualizada)
            .when()
            .put("/disciplinas/" + id)
            .then()
            .statusCode(200)
            .body("professor.id", equalTo(professorId.intValue()))
            .body("professor.nome", equalTo("Prof. Teste"));
    }
    @Test
    @Order(19)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    void testRemoverProfessorDaDisciplina() {
        // Criar disciplina com professor
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "IA Básica",
            "IAB001",
            cursoId,
            professorId
        );
        Integer extractedId = given()
            .contentType(ContentType.JSON)
            .body(disciplinaDto)
            .when()
            .post("/disciplinas")
            .then()
            .statusCode(201)
            .extract()
            .path("id");
        Long id = extractedId.longValue();
        // Atualizar para remover professor
        DisciplinaRequestDTO atualizada = new DisciplinaRequestDTO(
            "IA Básica",
            "IAB001",
            cursoId,
            null
        );
        given()
            .contentType(ContentType.JSON)
            .body(atualizada)
            .when()
            .put("/disciplinas/" + id)
            .then()
            .statusCode(200)
            .body("professor", nullValue());
    }
}
