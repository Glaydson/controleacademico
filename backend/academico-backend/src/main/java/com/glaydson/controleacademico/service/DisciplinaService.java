package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.rest.dto.DisciplinaRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class DisciplinaService {

    DisciplinaRepository disciplinaRepository;

    CursoRepository cursoRepository;

    public DisciplinaService(DisciplinaRepository disciplinaRepository, CursoRepository cursoRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.cursoRepository = cursoRepository;
    }

    public List<Disciplina> listarTodasDisciplinas() {
        return disciplinaRepository.listAll();
    }

    public Optional<Disciplina> buscarDisciplinaPorId(Long id) {
        return disciplinaRepository.findByIdOptional(id);
    }

    public Optional<Disciplina> buscarDisciplinaPorCodigo(String codigo) {
        return disciplinaRepository.find("codigo", codigo).firstResultOptional();
    }

    @Transactional
    public Disciplina criarDisciplina(DisciplinaRequestDTO disciplinaDto) { // Receives DTO
        if (disciplinaRepository.find("codigo", disciplinaDto.getCodigo()).count() > 0) {
            throw new BadRequestException("Já existe uma disciplina com o código " + disciplinaDto.getCodigo());
        }
        if (disciplinaRepository.find("nome", disciplinaDto.getNome()).count() > 0) {
            throw new BadRequestException("Já existe uma disciplina com o nome " + disciplinaDto.getNome());
        }

        Disciplina disciplina = new Disciplina();
        disciplina.setNome(disciplinaDto.getNome());
        disciplina.setCodigo(disciplinaDto.getCodigo());

        // Validate and associate courses
        Set<Curso> cursosParaAssociar = new HashSet<>();
        if (disciplinaDto.getCursoIds() != null && !disciplinaDto.getCursoIds().isEmpty()) {
            for (Long cursoId : disciplinaDto.getCursoIds()) {
                Curso cursoExistente = cursoRepository.findByIdOptional(cursoId)
                        .orElseThrow(() -> new NotFoundException("Curso com ID " + cursoId + " não encontrado."));
                cursosParaAssociar.add(cursoExistente);
            }
        }
        // Ensure bidirectional relationship is set
        for (Curso curso : cursosParaAssociar) {
            disciplina.addCurso(curso);
        }

        disciplinaRepository.persist(disciplina);
        return disciplina;
    }

    @Transactional
    public Disciplina atualizarDisciplina(Long id, DisciplinaRequestDTO disciplinaDto) { // Receives DTO
        Disciplina disciplinaExistente = disciplinaRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Disciplina com ID " + id + " não encontrada."));

        // Validate uniqueness
        if (!disciplinaExistente.getCodigo().equals(disciplinaDto.getCodigo()) &&
                disciplinaRepository.find("codigo = ?1 and id <> ?2", disciplinaDto.getCodigo(), id).count() > 0) {
            throw new BadRequestException("Já existe outra disciplina com o código " + disciplinaDto.getCodigo());
        }
        if (!disciplinaExistente.getNome().equals(disciplinaDto.getNome()) &&
                disciplinaRepository.find("nome = ?1 and id <> ?2", disciplinaDto.getNome(), id).count() > 0) {
            throw new BadRequestException("Já existe outra disciplina com o nome " + disciplinaDto.getNome());
        }

        disciplinaExistente.setNome(disciplinaDto.getNome());
        disciplinaExistente.setCodigo(disciplinaDto.getCodigo());

        // Update the list of associated courses
        Set<Curso> cursosAtuais = new HashSet<>(disciplinaExistente.getCursos());
        Set<Curso> novosCursos = new HashSet<>();

        if (disciplinaDto.getCursoIds() != null && !disciplinaDto.getCursoIds().isEmpty()) {
            for (Long cursoId : disciplinaDto.getCursoIds()) {
                Curso curso = cursoRepository.findByIdOptional(cursoId)
                        .orElseThrow(() -> new NotFoundException("Curso com ID " + cursoId + " não encontrado."));
                novosCursos.add(curso);
            }
        }

        // Remove old associations
        for (Curso cursoAntigo : cursosAtuais) {
            if (!novosCursos.contains(cursoAntigo)) {
                disciplinaExistente.removeCurso(cursoAntigo);
            }
        }

        // Add new associations
        for (Curso novoCurso : novosCursos) {
            if (!cursosAtuais.contains(novoCurso)) {
                disciplinaExistente.addCurso(novoCurso);
            }
        }

        return disciplinaExistente;
    }

    @Transactional
    public boolean deletarDisciplina(Long id) {
        Disciplina disciplina = disciplinaRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Disciplina com ID " + id + " não encontrada."));

        // Remove Many-to-Many associations before deleting
        for (Curso curso : new HashSet<>(disciplina.getCursos())) { // Use a copy
            curso.removeDisciplina(disciplina);
        }
        disciplina.getCursos().clear(); // Clear the set on the Disciplina side

        return disciplinaRepository.deleteById(id);
    }
}
