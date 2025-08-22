package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.domain.repository.ProfessorRepository;
import com.glaydson.controleacademico.rest.dto.DisciplinaRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DisciplinaService {

    DisciplinaRepository disciplinaRepository;
    CursoRepository cursoRepository;
    ProfessorRepository professorRepository;

    public DisciplinaService(DisciplinaRepository disciplinaRepository, CursoRepository cursoRepository, ProfessorRepository professorRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.cursoRepository = cursoRepository;
        this.professorRepository = professorRepository;
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
    public Disciplina criarDisciplina(DisciplinaRequestDTO disciplinaDto) {
        // Validate unique constraints
        if (disciplinaRepository.find("codigo", disciplinaDto.getCodigo()).count() > 0) {
            throw new BadRequestException("Já existe uma disciplina com o código " + disciplinaDto.getCodigo());
        }
        if (disciplinaRepository.find("nome", disciplinaDto.getNome()).count() > 0) {
            throw new BadRequestException("Já existe uma disciplina com o nome " + disciplinaDto.getNome());
        }

        // Validate and get the curso
        Curso curso = cursoRepository.findByIdOptional(disciplinaDto.getCursoId())
                .orElseThrow(() -> new NotFoundException("Curso com ID " + disciplinaDto.getCursoId() + " não encontrado."));

        // Create new disciplina
        Disciplina disciplina = new Disciplina(disciplinaDto.getNome(), disciplinaDto.getCodigo(), curso);
        // Handle professor assignment
        if (disciplinaDto.getProfessorId() != null) {
            var professor = professorRepository.findByIdOptional(disciplinaDto.getProfessorId())
                .orElseThrow(() -> new NotFoundException("Professor com ID " + disciplinaDto.getProfessorId() + " não encontrado."));
            disciplina.setProfessor(professor);
            professor.getDisciplinas().add(disciplina);
        }
        disciplinaRepository.persist(disciplina);
        return disciplina;
    }

    @Transactional
    public Disciplina atualizarDisciplina(Long id, DisciplinaRequestDTO disciplinaDto) {
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

        // Validate and get the new curso
        Curso novoCurso = cursoRepository.findByIdOptional(disciplinaDto.getCursoId())
                .orElseThrow(() -> new NotFoundException("Curso com ID " + disciplinaDto.getCursoId() + " não encontrado."));

        // Update disciplina properties
        disciplinaExistente.setNome(disciplinaDto.getNome());
        disciplinaExistente.setCodigo(disciplinaDto.getCodigo());
        disciplinaExistente.setCurso(novoCurso);
        // Handle professor update
        var oldProfessor = disciplinaExistente.getProfessor();
        if (disciplinaDto.getProfessorId() != null) {
            var newProfessor = professorRepository.findByIdOptional(disciplinaDto.getProfessorId())
                .orElseThrow(() -> new NotFoundException("Professor com ID " + disciplinaDto.getProfessorId() + " não encontrado."));
            if (oldProfessor != null && !oldProfessor.equals(newProfessor)) {
                oldProfessor.getDisciplinas().remove(disciplinaExistente);
            }
            disciplinaExistente.setProfessor(newProfessor);
            newProfessor.getDisciplinas().add(disciplinaExistente);
        } else {
            if (oldProfessor != null) {
                oldProfessor.getDisciplinas().remove(disciplinaExistente);
            }
            disciplinaExistente.setProfessor(null);
        }
        return disciplinaExistente;
    }

    @Transactional
    public boolean deletarDisciplina(Long id) {
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findByIdOptional(id);
        if (disciplinaOpt.isEmpty()) {
            return false;
        }
        Disciplina disciplina = disciplinaOpt.get();
        if (disciplina.getProfessor() != null) {
            disciplina.getProfessor().getDisciplinas().remove(disciplina);
        }
        return disciplinaRepository.deleteById(id);
    }
}
