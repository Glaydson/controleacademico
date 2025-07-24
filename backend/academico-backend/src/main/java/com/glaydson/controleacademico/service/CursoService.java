package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Coordenador;
import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.repository.CoordenadorRepository;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.rest.dto.CursoRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CursoService {

    CursoRepository cursoRepository;
    DisciplinaRepository disciplinaRepository;
    CoordenadorRepository coordenadorRepository;

    public CursoService(CursoRepository cursoRepository,
                        DisciplinaRepository disciplinaRepository,
                        CoordenadorRepository coordenadorRepository) {
        this.cursoRepository = cursoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    public List<Curso> listarTodosCursos() {
        // Fetching entities with their relationships to ensure DTO conversion is complete
        // For lazy-loaded relationships, you might need to explicitly fetch them here if not using a ViewGraph
        // E.g., cursoRepository.findAll().list().forEach(curso -> curso.getDisciplinas().size());
        return cursoRepository.listAll();
    }

    public Optional<Curso> buscarCursoPorId(Long id) {
        // Fetching entities with their relationships for complete DTO conversion
        return cursoRepository.findByIdOptional(id);
    }

    public Optional<Curso> buscarCursoPorCodigo(String codigo) {
        return cursoRepository.find("codigo", codigo).firstResultOptional();
    }

    @Transactional
    public Curso criarCurso(CursoRequestDTO cursoDto) { // Receives DTO
        if (cursoRepository.find("codigo", cursoDto.getCodigo()).count() > 0) {
            throw new BadRequestException("Já existe um curso com o código " + cursoDto.getCodigo());
        }
        if (cursoRepository.find("nome", cursoDto.getNome()).count() > 0) {
            throw new BadRequestException("Já existe um curso com o nome " + cursoDto.getNome());
        }

        Curso curso = new Curso();
        curso.setNome(cursoDto.getNome());
        curso.setCodigo(cursoDto.getCodigo());

        // Handle Disciplinas Many-to-Many
        Set<Disciplina> disciplinasParaAssociar = new HashSet<>();
        if (cursoDto.getDisciplinaIds() != null && !cursoDto.getDisciplinaIds().isEmpty()) {
            for (Long disciplinaId : cursoDto.getDisciplinaIds()) {
                Disciplina disciplina = disciplinaRepository.findByIdOptional(disciplinaId)
                        .orElseThrow(() -> new NotFoundException("Disciplina com ID " + disciplinaId + " não encontrada."));
                disciplinasParaAssociar.add(disciplina);
            }
        }
        // Ensure bidirectional relationship is set
        for (Disciplina disciplina : disciplinasParaAssociar) {
            curso.addDisciplina(disciplina);
        }

        // Handle Coordenador One-to-One
        if (cursoDto.getCoordenadorId() != null) {
            Coordenador coordenador = coordenadorRepository.findByIdOptional(cursoDto.getCoordenadorId())
                    .orElseThrow(() -> new NotFoundException("Coordenador com ID " + cursoDto.getCoordenadorId() + " não encontrado."));

            // Important: Check if the coordenador is already associated with another course
            // This assumes Coordenador has a 'curso' field and it's a unique relationship
            // If Coordenador's 'curso' field is `@OneToOne` with `unique=true` and `nullable=false`,
            // or if it's `mappedBy`, you need to handle this to avoid ConstraintViolation.
            if (coordenador.getCurso() != null) { // Check if the coordinator is already assigned
                throw new BadRequestException("O coordenador com ID " + coordenador.getId() + " já está associado ao curso '" + coordenador.getCurso().getNome() + "'.");
            }

            curso.setCoordenador(coordenador); // This will set the relationship on both sides
        }
        cursoRepository.persist(curso);
        return curso;
    }


    @Transactional
    public Curso atualizarCurso(Long id, CursoRequestDTO cursoDto) { // Receives DTO
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

        cursoExistente.setNome(cursoDto.getNome());
        cursoExistente.setCodigo(cursoDto.getCodigo());

        // Update Disciplinas Many-to-Many
        Set<Disciplina> disciplinasAtuais = new HashSet<>(cursoExistente.getDisciplinas());
        Set<Disciplina> novasDisciplinas = new HashSet<>();

        if (cursoDto.getDisciplinaIds() != null && !cursoDto.getDisciplinaIds().isEmpty()) {
            for (Long disciplinaId : cursoDto.getDisciplinaIds()) {
                Disciplina disciplina = disciplinaRepository.findByIdOptional(disciplinaId)
                        .orElseThrow(() -> new NotFoundException("Disciplina com ID " + disciplinaId + " não encontrada."));
                novasDisciplinas.add(disciplina);
            }
        }

        // Remove old associations
        for (Disciplina disciplinaAntiga : disciplinasAtuais) {
            if (!novasDisciplinas.contains(disciplinaAntiga)) {
                cursoExistente.removeDisciplina(disciplinaAntiga);
            }
        }

        // Add new associations
        for (Disciplina novaDisciplina : novasDisciplinas) {
            if (!disciplinasAtuais.contains(novaDisciplina)) {
                cursoExistente.addDisciplina(novaDisciplina);
            }
        }

        // Update Coordenador One-to-One
        if (cursoDto.getCoordenadorId() != null) {
            Coordenador novoCoordenador = coordenadorRepository.findByIdOptional(cursoDto.getCoordenadorId())
                    .orElseThrow(() -> new NotFoundException("Coordenador com ID " + cursoDto.getCoordenadorId() + " não encontrado."));

            // Logic to handle changing or setting a new coordinator
            // If there's an existing coordinator and it's different from the new one
            if (cursoExistente.getCoordenador() != null && !cursoExistente.getCoordenador().getId().equals(novoCoordenador.getId())) {
                // Check if the new coordinator is already associated with another course
                if (novoCoordenador.getCurso() != null && !novoCoordenador.getCurso().getId().equals(cursoExistente.getId())) {
                    throw new BadRequestException("O coordenador com ID " + novoCoordenador.getId() + " já está associado ao curso '" + novoCoordenador.getCurso().getNome() + "'.");
                }
                // Desassociate old coordinator from this course (if applicable)
                cursoExistente.getCoordenador().setCurso(null); // Clear the association on the old coordinator's side
            } else if (cursoExistente.getCoordenador() == null) {
                // If the course didn't have a coordinator, check if the new coordinator is already associated
                if (novoCoordenador.getCurso() != null) {
                    throw new BadRequestException("O coordenador com ID " + novoCoordenador.getId() + " já está associado ao curso '" + novoCoordenador.getCurso().getNome() + "'.");
                }
            }
            cursoExistente.setCoordenador(novoCoordenador); // Set the new coordinator
        } else {
            // If coordenadorId is null in DTO but course has a coordinator, desassociate it
            if (cursoExistente.getCoordenador() != null) {
                cursoExistente.getCoordenador().setCurso(null); // Clear the association on the coordinator's side
                cursoExistente.setCoordenador(null); // Clear on the course side
            }
        }

        return cursoExistente;
    }

    @Transactional
    public boolean deletarCurso(Long id) {
        Curso curso = cursoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Curso com ID " + id + " não encontrado."));

        // Clean up Many-to-Many relationships before deleting
        // If Disciplina is the owning side, it will manage the join table entries
        for (Disciplina disciplina : new HashSet<>(curso.getDisciplinas())) { // Use a copy to avoid ConcurrentModificationException
            disciplina.removeCurso(curso);
        }
        curso.getDisciplinas().clear(); // Clear the set on the Curso side

        // If Curso has a Coordenador and cascade/orphanRemoval is configured, it will handle deletion or disassociation.
        // Otherwise, explicitly handle it here if needed:
        if (curso.getCoordenador() != null) {
           curso.getCoordenador().setCurso(null); // Disassociate
         }
        // If you need to delete the coordenador if the course is deleted and it's an "orphan"
        // you would configure orphanRemoval = true on the @OneToOne in Curso entity.

        return cursoRepository.deleteById(id);
    }
}
