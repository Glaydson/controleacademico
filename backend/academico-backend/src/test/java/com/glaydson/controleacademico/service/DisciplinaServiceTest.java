package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.rest.dto.DisciplinaRequestDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisciplinaServiceTest {

    @Inject
    DisciplinaService disciplinaService;

    @Inject
    DisciplinaRepository disciplinaRepository;

    @Inject
    CursoRepository cursoRepository;

    private Curso curso1;
    private Curso curso2;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up existing data
        disciplinaRepository.deleteAll();
        cursoRepository.deleteAll();

        // Create test cursos
        curso1 = new Curso("Ciência da Computação", "CC001");
        curso1.persist();

        curso2 = new Curso("Engenharia de Software", "ES001");
        curso2.persist();
    }

    @Test
    public void testCriarDisciplinaComSucesso() {
        // Given
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Algoritmos e Estruturas de Dados",
            "AED001",
            curso1.id
        );

        // When
        Disciplina disciplinaCriada = disciplinaService.criarDisciplina(disciplinaDto);

        // Then
        assertNotNull(disciplinaCriada);
        assertNotNull(disciplinaCriada.id);
        assertEquals("Algoritmos e Estruturas de Dados", disciplinaCriada.nome);
        assertEquals("AED001", disciplinaCriada.codigo);
        assertNotNull(disciplinaCriada.curso);
        assertEquals(curso1.id, disciplinaCriada.curso.id);
        assertEquals("Ciência da Computação", disciplinaCriada.curso.nome);
    }

    @Test
    public void testCriarDisciplinaComCursoInexistente() {
        // Given
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Disciplina Teste",
            "TEST001",
            999L // ID inexistente
        );

        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> disciplinaService.criarDisciplina(disciplinaDto)
        );

        assertEquals("Curso com ID 999 não encontrado.", exception.getMessage());
    }

    @Test
    public void testCriarDisciplinaComCodigoJaExistente() {
        // Given - criar primeira disciplina
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Primeira Disciplina",
            "CODIGO001",
            curso1.id
        );
        disciplinaService.criarDisciplina(disciplina1);

        // Given - tentar criar segunda disciplina com mesmo código
        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Segunda Disciplina",
            "CODIGO001", // Mesmo código
            curso2.id
        );

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> disciplinaService.criarDisciplina(disciplina2)
        );

        assertEquals("Já existe uma disciplina com o código CODIGO001", exception.getMessage());
    }

    @Test
    public void testCriarDisciplinaComNomeJaExistente() {
        // Given - criar primeira disciplina
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Nome Repetido",
            "CODIGO001",
            curso1.id
        );
        disciplinaService.criarDisciplina(disciplina1);

        // Given - tentar criar segunda disciplina com mesmo nome
        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Nome Repetido", // Mesmo nome
            "CODIGO002",
            curso2.id
        );

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> disciplinaService.criarDisciplina(disciplina2)
        );

        assertEquals("Já existe uma disciplina com o nome Nome Repetido", exception.getMessage());
    }

    @Test
    public void testBuscarDisciplinaPorIdExistente() {
        // Given
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Banco de Dados",
            "BD001",
            curso1.id
        );
        Disciplina disciplinaCriada = disciplinaService.criarDisciplina(disciplinaDto);

        // When
        Optional<Disciplina> disciplinaEncontrada = disciplinaService.buscarDisciplinaPorId(disciplinaCriada.id);

        // Then
        assertTrue(disciplinaEncontrada.isPresent());
        assertEquals(disciplinaCriada.id, disciplinaEncontrada.get().id);
        assertEquals("Banco de Dados", disciplinaEncontrada.get().nome);
        assertEquals("BD001", disciplinaEncontrada.get().codigo);
        assertEquals(curso1.id, disciplinaEncontrada.get().curso.id);
    }

    @Test
    public void testBuscarDisciplinaPorIdInexistente() {
        // When
        Optional<Disciplina> disciplinaEncontrada = disciplinaService.buscarDisciplinaPorId(999L);

        // Then
        assertFalse(disciplinaEncontrada.isPresent());
    }

    @Test
    public void testBuscarDisciplinaPorCodigoExistente() {
        // Given
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Sistemas Operacionais",
            "SO001",
            curso1.id
        );
        disciplinaService.criarDisciplina(disciplinaDto);

        // When
        Optional<Disciplina> disciplinaEncontrada = disciplinaService.buscarDisciplinaPorCodigo("SO001");

        // Then
        assertTrue(disciplinaEncontrada.isPresent());
        assertEquals("Sistemas Operacionais", disciplinaEncontrada.get().nome);
        assertEquals("SO001", disciplinaEncontrada.get().codigo);
        assertEquals(curso1.id, disciplinaEncontrada.get().curso.id);
    }

    @Test
    public void testBuscarDisciplinaPorCodigoInexistente() {
        // When
        Optional<Disciplina> disciplinaEncontrada = disciplinaService.buscarDisciplinaPorCodigo("INEXISTENTE");

        // Then
        assertFalse(disciplinaEncontrada.isPresent());
    }

    @Test
    public void testListarTodasDisciplinas() {
        // Given - criar algumas disciplinas
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Matemática Discreta",
            "MAT001",
            curso1.id
        );
        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Arquitetura de Computadores",
            "ARQ001",
            curso2.id
        );

        disciplinaService.criarDisciplina(disciplina1);
        disciplinaService.criarDisciplina(disciplina2);

        // When
        List<Disciplina> disciplinas = disciplinaService.listarTodasDisciplinas();

        // Then
        assertNotNull(disciplinas);
        assertTrue(disciplinas.size() >= 2);

        boolean encontrouMat001 = disciplinas.stream()
            .anyMatch(d -> "MAT001".equals(d.codigo) && "Matemática Discreta".equals(d.nome));
        boolean encontrouArq001 = disciplinas.stream()
            .anyMatch(d -> "ARQ001".equals(d.codigo) && "Arquitetura de Computadores".equals(d.nome));

        assertTrue(encontrouMat001);
        assertTrue(encontrouArq001);
    }

    @Test
    public void testAtualizarDisciplinaComSucesso() {
        // Given - criar disciplina inicial
        DisciplinaRequestDTO disciplinaOriginal = new DisciplinaRequestDTO(
            "Programação I",
            "PROG001",
            curso1.id
        );
        Disciplina disciplinaCriada = disciplinaService.criarDisciplina(disciplinaOriginal);

        // Given - dados para atualização (incluindo mudança de curso)
        DisciplinaRequestDTO disciplinaAtualizada = new DisciplinaRequestDTO(
            "Programação Avançada",
            "PROG002",
            curso2.id // Mudando para outro curso
        );

        // When
        Disciplina disciplinaResult = disciplinaService.atualizarDisciplina(disciplinaCriada.id, disciplinaAtualizada);

        // Then
        assertNotNull(disciplinaResult);
        assertEquals(disciplinaCriada.id, disciplinaResult.id);
        assertEquals("Programação Avançada", disciplinaResult.nome);
        assertEquals("PROG002", disciplinaResult.codigo);
        assertEquals(curso2.id, disciplinaResult.curso.id);
        assertEquals("Engenharia de Software", disciplinaResult.curso.nome);
    }

    @Test
    public void testAtualizarDisciplinaInexistente() {
        // Given
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Disciplina Inexistente",
            "INX001",
            curso1.id
        );

        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> disciplinaService.atualizarDisciplina(999L, disciplinaDto)
        );

        assertEquals("Disciplina com ID 999 não encontrada.", exception.getMessage());
    }

    @Test
    public void testAtualizarDisciplinaComCodigoJaExistente() {
        // Given - criar duas disciplinas
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Primeira Disciplina",
            "CODIGO001",
            curso1.id
        );
        Disciplina d1 = disciplinaService.criarDisciplina(disciplina1);

        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Segunda Disciplina",
            "CODIGO002",
            curso2.id
        );
        Disciplina d2 = disciplinaService.criarDisciplina(disciplina2);

        // Given - tentar atualizar segunda disciplina com código da primeira
        DisciplinaRequestDTO disciplinaAtualizada = new DisciplinaRequestDTO(
            "Segunda Disciplina Atualizada",
            "CODIGO001", // Código já existe
            curso2.id
        );

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> disciplinaService.atualizarDisciplina(d2.id, disciplinaAtualizada)
        );

        assertEquals("Já existe outra disciplina com o código CODIGO001", exception.getMessage());
    }

    @Test
    public void testAtualizarDisciplinaComNomeJaExistente() {
        // Given - criar duas disciplinas
        DisciplinaRequestDTO disciplina1 = new DisciplinaRequestDTO(
            "Nome Existente",
            "CODIGO001",
            curso1.id
        );
        Disciplina d1 = disciplinaService.criarDisciplina(disciplina1);

        DisciplinaRequestDTO disciplina2 = new DisciplinaRequestDTO(
            "Outro Nome",
            "CODIGO002",
            curso2.id
        );
        Disciplina d2 = disciplinaService.criarDisciplina(disciplina2);

        // Given - tentar atualizar segunda disciplina com nome da primeira
        DisciplinaRequestDTO disciplinaAtualizada = new DisciplinaRequestDTO(
            "Nome Existente", // Nome já existe
            "CODIGO003",
            curso2.id
        );

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> disciplinaService.atualizarDisciplina(d2.id, disciplinaAtualizada)
        );

        assertEquals("Já existe outra disciplina com o nome Nome Existente", exception.getMessage());
    }

    @Test
    public void testAtualizarDisciplinaComCursoInexistente() {
        // Given - criar disciplina
        DisciplinaRequestDTO disciplinaOriginal = new DisciplinaRequestDTO(
            "Disciplina Teste",
            "TEST001",
            curso1.id
        );
        Disciplina disciplinaCriada = disciplinaService.criarDisciplina(disciplinaOriginal);

        // Given - tentar atualizar com curso inexistente
        DisciplinaRequestDTO disciplinaAtualizada = new DisciplinaRequestDTO(
            "Disciplina Teste Atualizada",
            "TEST002",
            999L // Curso inexistente
        );

        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> disciplinaService.atualizarDisciplina(disciplinaCriada.id, disciplinaAtualizada)
        );

        assertEquals("Curso com ID 999 não encontrado.", exception.getMessage());
    }

    @Test
    public void testDeletarDisciplinaComSucesso() {
        // Given
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Disciplina para Deletar",
            "DEL001",
            curso1.id
        );
        Disciplina disciplinaCriada = disciplinaService.criarDisciplina(disciplinaDto);

        // When
        boolean resultado = disciplinaService.deletarDisciplina(disciplinaCriada.id);

        // Then
        assertTrue(resultado);

        // Verificar se foi realmente deletada
        Optional<Disciplina> disciplinaBuscada = disciplinaService.buscarDisciplinaPorId(disciplinaCriada.id);
        assertFalse(disciplinaBuscada.isPresent());
    }

    @Test
    public void testDeletarDisciplinaInexistente() {
        // When
        boolean resultado = disciplinaService.deletarDisciplina(999L);

        // Then
        assertFalse(resultado);
    }

    @Test
    public void testRelacionamentoCursoMantoIntegridade() {
        // Given - criar disciplina associada a um curso
        DisciplinaRequestDTO disciplinaDto = new DisciplinaRequestDTO(
            "Teste Relacionamento",
            "REL001",
            curso1.id
        );
        Disciplina disciplina = disciplinaService.criarDisciplina(disciplinaDto);

        // When - buscar a disciplina e verificar o relacionamento
        Optional<Disciplina> disciplinaEncontrada = disciplinaService.buscarDisciplinaPorId(disciplina.id);

        // Then - verificar integridade do relacionamento
        assertTrue(disciplinaEncontrada.isPresent());
        assertNotNull(disciplinaEncontrada.get().curso);
        assertEquals(curso1.id, disciplinaEncontrada.get().curso.id);
        assertEquals(curso1.nome, disciplinaEncontrada.get().curso.nome);
        assertEquals(curso1.codigo, disciplinaEncontrada.get().curso.codigo);
    }
}
