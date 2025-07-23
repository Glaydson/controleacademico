package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CursoService {

    CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<Curso> listarTodosCursos() {
        return cursoRepository.listAll();
    }

    public Optional<Curso> buscarCursoPorId(Long id) {
        return cursoRepository.findByIdOptional(id);
    }

    public Optional<Curso> buscarCursoPorCodigo(String codigo) {
        return cursoRepository.find("codigo", codigo).firstResultOptional();
    }

    @Transactional
    public Curso criarCurso(Curso curso) {
        if (curso.id != null) {
            throw new BadRequestException("ID deve ser nulo para criar um novo curso.");
        }
        if (cursoRepository.find("codigo", curso.getCodigo()).count() > 0) {
            throw new BadRequestException("Já existe um curso com o código " + curso.getCodigo());
        }
        if (cursoRepository.find("nome", curso.getNome()).count() > 0) {
            throw new BadRequestException("Já existe um curso com o nome " + curso.getNome());
        }
        cursoRepository.persist(curso);
        return curso;
    }

    @Transactional
    public Curso atualizarCurso(Long id, Curso cursoAtualizado) {
        Curso cursoExistente = cursoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Curso com ID " + id + " não encontrado."));

        // Validação de unicidade para código e nome (se alterados)
        if (!cursoExistente.getCodigo().equals(cursoAtualizado.getCodigo()) &&
                cursoRepository.find("codigo", cursoAtualizado.getCodigo()).count() > 0) {
            throw new BadRequestException("Já existe outro curso com o código " + cursoAtualizado.getCodigo());
        }
        if (!cursoExistente.getNome().equals(cursoAtualizado.getNome()) &&
                cursoRepository.find("nome", cursoAtualizado.getNome()).count() > 0) {
            throw new BadRequestException("Já existe outro curso com o nome " + cursoAtualizado.getNome());
        }

        cursoExistente.setNome(cursoAtualizado.getNome());
        cursoExistente.setCodigo(cursoAtualizado.getCodigo());
        return cursoExistente;
    }

    @Transactional
    public boolean deletarCurso(Long id) {
        // Você pode adicionar lógica aqui para verificar se o curso está em uso (ex: alunos matriculados, matrizes curriculares)
        // antes de permitir a deleção.
        return cursoRepository.deleteById(id);
    }
}
