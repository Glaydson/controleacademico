package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
    public Disciplina criarDisciplina(Disciplina disciplina) {
        if (disciplina.id != null) {
            throw new BadRequestException("ID deve ser nulo para criar uma nova disciplina.");
        }
        if (disciplinaRepository.find("codigo", disciplina.getCodigo()).count() > 0) {
            throw new BadRequestException("Já existe uma disciplina com o código " + disciplina.getCodigo());
        }
        if (disciplinaRepository.find("nome", disciplina.getNome()).count() > 0) {
            throw new BadRequestException("Já existe uma disciplina com o nome " + disciplina.getNome());
        }

        // Valida e associa cursos
        Set<Curso> cursosGerenciados = new HashSet<>();
        if (disciplina.getCursos() != null && !disciplina.getCursos().isEmpty()) {
            for (Curso curso : disciplina.getCursos()) {
                if (curso.id == null) {
                    throw new BadRequestException("IDs de curso devem ser fornecidos para associar uma disciplina.");
                }
                Curso cursoExistente = cursoRepository.findByIdOptional(curso.id)
                        .orElseThrow(() -> new NotFoundException("Curso com ID " + curso.id + " não encontrado."));
                cursosGerenciados.add(cursoExistente);
            }
        }
        disciplina.setCursos(cursosGerenciados); // Associa os cursos gerenciados

        disciplinaRepository.persist(disciplina);
        return disciplina;
    }

    @Transactional
    public Disciplina atualizarDisciplina(Long id, Disciplina disciplinaAtualizada) {
        Disciplina disciplinaExistente = disciplinaRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Disciplina com ID " + id + " não encontrada."));

        // Validação de unicidade
        if (!disciplinaExistente.getCodigo().equals(disciplinaAtualizada.getCodigo()) &&
                disciplinaRepository.find("codigo", disciplinaAtualizada.getCodigo()).count() > 0) {
            throw new BadRequestException("Já existe outra disciplina com o código " + disciplinaAtualizada.getCodigo());
        }
        if (!disciplinaExistente.getNome().equals(disciplinaAtualizada.getNome()) &&
                disciplinaRepository.find("nome", disciplinaAtualizada.getNome()).count() > 0) {
            throw new BadRequestException("Já existe outra disciplina com o nome " + disciplinaAtualizada.getNome());
        }

        disciplinaExistente.setNome(disciplinaAtualizada.getNome());
        disciplinaExistente.setCodigo(disciplinaAtualizada.getCodigo());

        // Atualiza a lista de cursos associados
        Set<Curso> cursosAtualizados = new HashSet<>();
        if (disciplinaAtualizada.getCursos() != null && !disciplinaAtualizada.getCursos().isEmpty()) {
            for (Curso curso : disciplinaAtualizada.getCursos()) {
                if (curso.id == null) {
                    throw new BadRequestException("IDs de curso devem ser fornecidos para associar uma disciplina.");
                }
                Curso cursoExistente = cursoRepository.findByIdOptional(curso.id)
                        .orElseThrow(() -> new NotFoundException("Curso com ID " + curso.id + " não encontrado."));
                cursosAtualizados.add(cursoExistente);
            }
        }
        disciplinaExistente.setCursos(cursosAtualizados); // Atualiza a coleção

        return disciplinaExistente;
    }

    @Transactional
    public boolean deletarDisciplina(Long id) {
        // Adicione lógica para verificar se a disciplina está em uso (ex: em Matrizes Curriculares)
        return disciplinaRepository.deleteById(id);
    }
}
