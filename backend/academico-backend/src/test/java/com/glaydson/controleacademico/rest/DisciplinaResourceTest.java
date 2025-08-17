package com.glaydson.controleacademico.rest;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.rest.dto.DisciplinaRequestDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
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
public class DisciplinaResourceTest {

    private Long cursoId;
    private Long segundoCursoId;
    private Long disciplinaId;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up existing data
        Disciplina.deleteAll();
        Curso.deleteAll();

        // Create test cursos
        Curso curso1 = new Curso("Ciência da Computação", "CC001");
        curso1.persist();
        cursoId = curso1.id;

        Curso curso2 = new Curso("Engenharia de Software", "ES001");
        curso2.persist();
        segundoCursoId = curso2.id;
    }

    @Test
    @Order(1)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    public void testCriarDisciplinaComCursoAssociado() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Algoritmos e Estruturas de Dados",
            "AED001",
            cursoId
        );

        Integer extractedId = given()
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
            .body("curso.codigo", equalTo("CC001"))
            .extract()
            .path("id");

        disciplinaId = extractedId.longValue();
    }

    @Test
    @Order(2)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    public void testCriarDisciplinaSemCurso() {
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
    public void testCriarDisciplinaComCursoInexistente() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Programação Orientada a Objetos",
            "POO001",
            999L // ID inexistente
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
    public void testCriarDisciplinaComCodigoRepetido() {
        // Primeiro, criar uma disciplina
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Banco de Dados I",
            "BD001",
            cursoId
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
            segundoCursoId
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
    public void testCriarDisciplinaComNomeRepetido() {
        // Primeiro, criar uma disciplina
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Inteligência Artificial",
            "IA001",
            cursoId
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
            segundoCursoId
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
    public void testBuscarDisciplinaPorId() {
        // Criar disciplina para teste
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Sistemas Operacionais",
            "SO001",
            cursoId
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
    public void testBuscarDisciplinaPorIdInexistente() {
        given()
            .when()
            .get("/disciplinas/999")
            .then()
            .statusCode(404); // Not Found
    }

    @Test
    @Order(8)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    public void testBuscarDisciplinaPorCodigo() {
        // Criar disciplina para teste (requires COORDENADOR)
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Redes de Computadores",
            "RC001",
            cursoId
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
    public void testBuscarDisciplinaPorCodigoInexistente() {
        given()
            .when()
            .get("/disciplinas/codigo/INEXISTENTE")
            .then()
            .statusCode(404); // Not Found
    }

    @Test
    @Order(10)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    public void testListarTodasDisciplinas() {
        // Criar algumas disciplinas para teste (requires COORDENADOR)
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Compiladores",
            "COMP001",
            cursoId
        );

        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Engenharia de Software",
            "ENGS001",
            segundoCursoId
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
    public void testAtualizarDisciplina() {
        // Criar disciplina para teste
        DisciplinaRequestDTO disciplinaOriginal = new DisciplinaRequestDTO(
            "Programação Web",
            "WEB001",
            cursoId
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
            segundoCursoId // Mudando para outro curso
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
    public void testAtualizarDisciplinaInexistente() {
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Disciplina Inexistente",
            "INX001",
            cursoId
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
    public void testDeletarDisciplina() {
        // Criar disciplina para teste
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Segurança da Informação",
            "SEG001",
            cursoId
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
    public void testDeletarDisciplinaInexistente() {
        given()
            .when()
            .delete("/disciplinas/999")
            .then()
            .statusCode(404); // Not Found
    }

    @Test
    @Order(15)
    @TestSecurity(user = "testuser", roles = {"COORDENADOR"})
    public void testValidacaoCamposObrigatorios() {
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
}
