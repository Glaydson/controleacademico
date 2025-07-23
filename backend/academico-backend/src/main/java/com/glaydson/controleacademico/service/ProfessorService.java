package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Professor;
import com.glaydson.controleacademico.domain.repository.ProfessorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProfessorService {

    ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public List<Professor> listarTodosProfessores() {
        return professorRepository.listAll();
    }

    public Optional<Professor> buscarProfessorPorId(Long id) {
        return professorRepository.findByIdOptional(id);
    }

    public Optional<Professor> buscarProfessorPorRegistro(String registro) {
        return professorRepository.find("registro", registro).firstResultOptional();
    }

    @Transactional
    public Professor criarProfessor(Professor professor) {
        if (professor.getId() != null) {
            throw new BadRequestException("ID deve ser nulo para criar um novo professor.");
        }
        // Exemplo de validação de negócio: garantir que o registro seja único
        if (professorRepository.find("registro", professor.getRegistro()).count() > 0) {
            throw new BadRequestException("Já existe um professor com o registro " + professor.getRegistro());
        }
        professorRepository.persist(professor);
        return professor;
    }

    @Transactional
    public Professor atualizarProfessor(Long id, Professor professorAtualizado) {
        Professor professorExistente = professorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Professor com ID " + id + " não encontrado."));

        // Atualiza os campos
        professorExistente.setNome(professorAtualizado.getNome());
        professorExistente.setRegistro(professorAtualizado.getRegistro());

        // O Panache detecta as alterações e as persiste na transação
        return professorExistente;
    }

    @Transactional
    public boolean deletarProfessor(Long id) {
        return professorRepository.deleteById(id);
    }
}
