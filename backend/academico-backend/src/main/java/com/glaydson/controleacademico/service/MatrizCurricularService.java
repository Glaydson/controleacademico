package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.model.MatrizCurricular;
import com.glaydson.controleacademico.domain.model.Semestre;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.domain.repository.MatrizCurricularRepository;
import com.glaydson.controleacademico.domain.repository.SemestreRepository;
import com.glaydson.controleacademico.rest.dto.MatrizCurricularRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class MatrizCurricularService {

    MatrizCurricularRepository matrizCurricularRepository;
    CursoRepository cursoRepository;
    SemestreRepository semestreRepository;
    DisciplinaRepository disciplinaRepository;

    public MatrizCurricularService(MatrizCurricularRepository matrizCurricularRepository,
                                   CursoRepository cursoRepository, DisciplinaRepository disciplinaRepository,
                                   SemestreRepository semestreRepository) {
        this.matrizCurricularRepository = matrizCurricularRepository;
        this.cursoRepository = cursoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.semestreRepository = semestreRepository;
    }

    public List<MatrizCurricular> listarTodasMatrizesCurriculares() {
        return matrizCurricularRepository.listAll();
    }

    public Optional<MatrizCurricular> buscarMatrizCurricularPorId(Long id) {
        return matrizCurricularRepository.findByIdOptional(id);
    }

    @Transactional
    public MatrizCurricular criarMatrizCurricular(MatrizCurricularRequestDTO matrizDto) {// Recebe DTO

        if (matrizDto.getCursoId() == null ) {
            throw new BadRequestException("Uma matriz curricular deve estar associada a um curso válido.");
        }
        if (matrizDto.getSemestreId() == null ) {
            throw new BadRequestException("Uma matriz curricular deve estar associada a um semestre válido.");
        }

        // Valida e busca as entidades gerenciadas
        Curso cursoExistente = cursoRepository.findByIdOptional(matrizDto.cursoId) // Usa cursoId do DTO
                .orElseThrow(() -> new NotFoundException("Curso com ID " + matrizDto.cursoId + " não encontrado."));
        Semestre semestreExistente = semestreRepository.findByIdOptional(matrizDto.semestreId) // Usa semestreId do DTO
                .orElseThrow(() -> new NotFoundException("Semestre com ID " + matrizDto.semestreId + " não encontrado."));

        // Garante que não haja duplicidade de matriz curricular para o mesmo curso e semestre
        if (matrizCurricularRepository.find("curso = ?1 and semestre = ?2", cursoExistente, semestreExistente).count() > 0) {
            throw new BadRequestException("Já existe uma matriz curricular para o Curso '" + cursoExistente.getNome() + "' e Semestre '" + semestreExistente.getPeriodo() + "'.");
        }

        MatrizCurricular matrizCurricular = new MatrizCurricular(); // Nova entidade
        matrizCurricular.setCurso(cursoExistente);
        matrizCurricular.setSemestre(semestreExistente);

        // Associa disciplinas
        Set<Disciplina> disciplinasGerenciadas = new HashSet<>();
        if (matrizDto.disciplinaIds != null && !matrizDto.disciplinaIds.isEmpty()) { // Usa disciplinaIds do DTO
            for (Long disciplinaId : matrizDto.disciplinaIds) {
                Disciplina disciplinaExistente = disciplinaRepository.findByIdOptional(disciplinaId)
                        .orElseThrow(() -> new NotFoundException("Disciplina com ID " + disciplinaId + " não encontrada."));
                disciplinasGerenciadas.add(disciplinaExistente);
            }
        }
        matrizCurricular.setDisciplinas(disciplinasGerenciadas);

        matrizCurricularRepository.persist(matrizCurricular);
        return matrizCurricular; // Retorna a entidade persistida
    }


    @Transactional
    public MatrizCurricular atualizarMatrizCurricular(Long id, MatrizCurricularRequestDTO matrizDto) { // Recebe DTO
        MatrizCurricular matrizExistente = matrizCurricularRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Matriz Curricular com ID " + id + " não encontrada."));

        // Atualiza curso e semestre se fornecidos (e valida)
        if (matrizDto.cursoId != null) {
            Curso novoCurso = cursoRepository.findByIdOptional(matrizDto.cursoId)
                    .orElseThrow(() -> new NotFoundException("Curso com ID " + matrizDto.cursoId + " não encontrado."));
            matrizExistente.setCurso(novoCurso);
        }
        if (matrizDto.semestreId != null) {
            Semestre novoSemestre = semestreRepository.findByIdOptional(matrizDto.semestreId)
                    .orElseThrow(() -> new NotFoundException("Semestre com ID " + matrizDto.semestreId + " não encontrado."));
            matrizExistente.setSemestre(novoSemestre);
        }

        // Revalida unicidade caso curso ou semestre tenham sido alterados
        if (matrizCurricularRepository.find("curso = ?1 and semestre = ?2 and id <> ?3", matrizExistente.getCurso(), matrizExistente.getSemestre(), matrizExistente.id).count() > 0) {
            throw new BadRequestException("Já existe outra matriz curricular com o mesmo Curso e Semestre.");
        }

        // Atualiza a lista de disciplinas
        Set<Disciplina> disciplinasAtualizadas = new HashSet<>();
        if (matrizDto.disciplinaIds != null && !matrizDto.disciplinaIds.isEmpty()) {
            for (Long disciplinaId : matrizDto.disciplinaIds) {
                Disciplina disciplinaExistente = disciplinaRepository.findByIdOptional(disciplinaId)
                        .orElseThrow(() -> new NotFoundException("Disciplina com ID " + disciplinaId + " não encontrada."));
                disciplinasAtualizadas.add(disciplinaExistente);
            }
        }
        matrizExistente.setDisciplinas(disciplinasAtualizadas);

        return matrizExistente; // Retorna a entidade atualizada
    }

    @Transactional
    public boolean deletarMatrizCurricular(Long id) {
        return matrizCurricularRepository.deleteById(id);
    }
}