package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Coordenador;
import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.repository.CoordenadorRepository;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.rest.dto.CursoRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CursoService {

    CursoRepository cursoRepository;
    CoordenadorRepository coordenadorRepository;

    public CursoService(CursoRepository cursoRepository, CoordenadorRepository coordenadorRepository) {
        this.cursoRepository = cursoRepository;
        this.coordenadorRepository = coordenadorRepository;
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
    public Curso criarCurso(CursoRequestDTO cursoDto) {
        // Validate unique constraints
        if (cursoRepository.find("codigo", cursoDto.getCodigo()).count() > 0) {
            throw new BadRequestException("Já existe um curso com o código " + cursoDto.getCodigo());
        }
        if (cursoRepository.find("nome", cursoDto.getNome()).count() > 0) {
            throw new BadRequestException("Já existe um curso com o nome " + cursoDto.getNome());
        }

        Curso curso = new Curso(cursoDto.getNome(), cursoDto.getCodigo());

        // Handle Coordenador One-to-One (optional)
        if (cursoDto.getCoordenadorId() != null) {
            Coordenador coordenador = coordenadorRepository.findByIdOptional(cursoDto.getCoordenadorId())
                    .orElseThrow(() -> new NotFoundException("Coordenador com ID " + cursoDto.getCoordenadorId() + " não encontrado."));

            // Check if the coordenador is already associated with another course
            if (coordenador.getCurso() != null) {
                throw new BadRequestException("O coordenador com ID " + coordenador.getId() + " já está associado ao curso '" + coordenador.getCurso().getNome() + "'.");
            }

            curso.setCoordenador(coordenador);
            coordenador.setCurso(curso); // Ensure bidirectional relationship
        }

        cursoRepository.persist(curso);
        return curso;
    }

    @Transactional
    public Curso atualizarCurso(Long id, CursoRequestDTO cursoDto) {
        Curso cursoExistente = cursoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Curso com ID " + id + " não encontrado."));

        // Validate uniqueness for code and name (if changed)
        if (!cursoExistente.getCodigo().equals(cursoDto.getCodigo()) &&
                cursoRepository.find("codigo = ?1 and id <> ?2", cursoDto.getCodigo(), id).count() > 0) {
            throw new BadRequestException("Já existe outro curso com o código " + cursoDto.getCodigo());
        }
        if (!cursoExistente.getNome().equals(cursoDto.getNome()) &&
                cursoRepository.find("nome = ?1 and id <> ?2", cursoDto.getNome(), id).count() > 0) {
            throw new BadRequestException("Já existe outro curso com o nome " + cursoDto.getNome());
        }

        // Update basic properties
        cursoExistente.setNome(cursoDto.getNome());
        cursoExistente.setCodigo(cursoDto.getCodigo());

        // Update Coordenador One-to-One
        if (cursoDto.getCoordenadorId() != null) {
            Coordenador novoCoordenador = coordenadorRepository.findByIdOptional(cursoDto.getCoordenadorId())
                    .orElseThrow(() -> new NotFoundException("Coordenador com ID " + cursoDto.getCoordenadorId() + " não encontrado."));

            // If there's an existing coordinator and it's different from the new one
            if (cursoExistente.getCoordenador() != null && !cursoExistente.getCoordenador().getId().equals(novoCoordenador.getId())) {
                // Check if the new coordinator is already associated with another course
                if (novoCoordenador.getCurso() != null && !novoCoordenador.getCurso().getId().equals(cursoExistente.getId())) {
                    throw new BadRequestException("O coordenador com ID " + novoCoordenador.getId() + " já está associado ao curso '" + novoCoordenador.getCurso().getNome() + "'.");
                }
                // Disassociate old coordinator
                cursoExistente.getCoordenador().setCurso(null);
            } else if (cursoExistente.getCoordenador() == null) {
                // If the course didn't have a coordinator, check if the new coordinator is available
                if (novoCoordenador.getCurso() != null) {
                    throw new BadRequestException("O coordenador com ID " + novoCoordenador.getId() + " já está associado ao curso '" + novoCoordenador.getCurso().getNome() + "'.");
                }
            }
            cursoExistente.setCoordenador(novoCoordenador);
            novoCoordenador.setCurso(cursoExistente); // Ensure bidirectional relationship
        } else {
            // If coordenadorId is null in DTO but course has a coordinator, disassociate it
            if (cursoExistente.getCoordenador() != null) {
                cursoExistente.getCoordenador().setCurso(null);
                cursoExistente.setCoordenador(null);
            }
        }

        return cursoExistente;
    }

    @Transactional
    public boolean deletarCurso(Long id) {
        Curso curso = cursoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Curso com ID " + id + " não encontrado."));

        // With the new Many-to-One relationship and CascadeType.ALL configuration,
        // all disciplinas associated with this curso will be automatically deleted
        // due to the cascade setting in the @OneToMany annotation

        // Handle Coordenador relationship
        if (curso.getCoordenador() != null) {
           curso.getCoordenador().setCurso(null); // Disassociate
        }

        return cursoRepository.deleteById(id);
    }
}
