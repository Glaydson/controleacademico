package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Coordenador;
import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.repository.CoordenadorRepository;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.rest.dto.CoordenadorRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CoordenadorService {

    public static final String NAO_ENCONTRADO = " não encontrado.";
    CoordenadorRepository coordenadorRepository;
    CursoRepository cursoRepository;

    public CoordenadorService(CoordenadorRepository coordenadorRepository, CursoRepository cursoRepository) {
        this.coordenadorRepository = coordenadorRepository;
        this.cursoRepository = cursoRepository;
    }

    public List<Coordenador> listarTodosCoordenadores() {
        return coordenadorRepository.listAll();
    }

    public Optional<Coordenador> buscarCoordenadorPorId(Long id) {
        return coordenadorRepository.findByIdOptional(id);
    }

    public Optional<Coordenador> buscarCoordenadorPorMatricula(String matricula) {
        return coordenadorRepository.find("matricula", matricula).firstResultOptional();
    }

    @Transactional
    public Coordenador criarCoordenador(CoordenadorRequestDTO coordenadorDto) { // Recebe o DTO
        if (coordenadorRepository.find("matricula", coordenadorDto.getRegistro()).count() > 0) {
            throw new BadRequestException("Já existe um coordenador com a matrícula " + coordenadorDto.getRegistro());
        }

        // Valida se o curso já tem um coordenador
        Curso cursoExistente = cursoRepository.findByIdOptional(coordenadorDto.cursoId)
                .orElseThrow(() -> new NotFoundException("Curso com ID " + coordenadorDto.cursoId + NAO_ENCONTRADO));

        if (coordenadorRepository.find("curso", cursoExistente).count() > 0) {
            throw new BadRequestException("O Curso '" + cursoExistente.nome + "' já possui um coordenador.");
        }


        Coordenador coordenador = new Coordenador(); // Cria a entidade
        coordenador.nome = coordenadorDto.nome;
        coordenador.registro = coordenadorDto.getRegistro();
        coordenador.curso = cursoExistente; // Associa a entidade Curso gerenciada

        coordenadorRepository.persist(coordenador);
        return coordenador;
    }

    @Transactional
    public Coordenador atualizarCoordenador(Long id, CoordenadorRequestDTO coordenadorDto) { // Pode reutilizar o DTO de criação para atualização simples
        Coordenador coordenadorExistente = coordenadorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Coordenador com ID " + id + NAO_ENCONTRADO));

        // Validação de unicidade para matrícula (se alterada)
        if (!coordenadorExistente.getRegistro().equals(coordenadorDto.getRegistro()) &&
                coordenadorRepository.find("matriculaFuncional", coordenadorDto.getRegistro()).count() > 0) {
            throw new BadRequestException("Já existe outro coordenador com o registro " + coordenadorDto.getRegistro());
        }

        coordenadorExistente.nome = coordenadorDto.nome;
        coordenadorExistente.registro = coordenadorDto.getRegistro();

        // Lógica de atualização de curso
        if (coordenadorDto.cursoId != null) {
            Curso novoCurso = cursoRepository.findByIdOptional(coordenadorDto.cursoId)
                    .orElseThrow(() -> new NotFoundException("Curso com ID " + coordenadorDto.cursoId + NAO_ENCONTRADO));

            // Verifica se o novo curso já tem um coordenador (diferente do coordenador atual)
            // Usamos como regra que a alteração do coordenador do curso deve ser feita no curso.
            if (!coordenadorExistente.curso.equals(novoCurso) &&
                    coordenadorRepository.find("curso = ?1 and id <> ?2", novoCurso, id).count() > 0) {
                throw new BadRequestException("O Curso '" + novoCurso.nome + "' já possui um coordenador.");
            }

            coordenadorExistente.curso = novoCurso;
        } else {
            throw new BadRequestException("O ID do curso é obrigatório para atualização do coordenador.");
        }

        return coordenadorExistente;
    }

    @Transactional
    public boolean deletarCoordenador(Long id) {
        return coordenadorRepository.deleteById(id);
    }
}
