package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.model.MatrizCurricular;
import com.glaydson.controleacademico.domain.model.Semestre;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.domain.repository.MatrizCurricularRepository;
import com.glaydson.controleacademico.domain.repository.SemestreRepository;
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
    public MatrizCurricular criarMatrizCurricular(MatrizCurricular matrizCurricular) {
        if (matrizCurricular.id != null) {
            throw new BadRequestException("ID deve ser nulo para criar uma nova matriz curricular.");
        }
        if (matrizCurricular.getCurso() == null || matrizCurricular.getCurso().id == null) {
            throw new BadRequestException("Uma matriz curricular deve estar associada a um curso válido.");
        }
        if (matrizCurricular.getSemestre() == null || matrizCurricular.getSemestre().id == null) {
            throw new BadRequestException("Uma matriz curricular deve estar associada a um semestre válido.");
        }

        // Valida e busca as entidades gerenciadas
        Curso cursoExistente = cursoRepository.findByIdOptional(matrizCurricular.getCurso().id)
                .orElseThrow(() -> new NotFoundException("Curso com ID " + matrizCurricular.getCurso().id + " não encontrado."));
        Semestre semestreExistente = semestreRepository.findByIdOptional(matrizCurricular.getSemestre().id)
                .orElseThrow(() -> new NotFoundException("Semestre com ID " + matrizCurricular.getSemestre().id + " não encontrado."));

        // Garante que não haja duplicidade de matriz curricular para o mesmo curso e semestre
        if (matrizCurricularRepository.find("curso = ?1 and semestre = ?2", cursoExistente, semestreExistente).count() > 0) {
            throw new BadRequestException("Já existe uma matriz curricular para o Curso '" + cursoExistente.getNome() + "' e Semestre '" + semestreExistente.getNome() + "'.");
        }

        matrizCurricular.setCurso(cursoExistente);
        matrizCurricular.setSemestre(semestreExistente);

        // Associa disciplinas
        Set<Disciplina> disciplinasGerenciadas = new HashSet<>();
        if (matrizCurricular.getDisciplinas() != null && !matrizCurricular.getDisciplinas().isEmpty()) {
            for (Disciplina disciplina : matrizCurricular.getDisciplinas()) {
                if (disciplina.id == null) {
                    throw new BadRequestException("IDs de disciplina devem ser fornecidos para associar à matriz.");
                }
                Disciplina disciplinaExistente = disciplinaRepository.findByIdOptional(disciplina.id)
                        .orElseThrow(() -> new NotFoundException("Disciplina com ID " + disciplina.id + " não encontrada."));
                disciplinasGerenciadas.add(disciplinaExistente);
            }
        }
        matrizCurricular.setDisciplinas(disciplinasGerenciadas);

        matrizCurricularRepository.persist(matrizCurricular);
        return matrizCurricular;
    }

    @Transactional
    public MatrizCurricular atualizarMatrizCurricular(Long id, MatrizCurricular matrizAtualizada) {
        MatrizCurricular matrizExistente = matrizCurricularRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Matriz Curricular com ID " + id + " não encontrada."));

        // Atualiza curso e semestre se fornecidos (e valida)
        if (matrizAtualizada.getCurso() != null && matrizAtualizada.getCurso().id != null) {
            Curso novoCurso = cursoRepository.findByIdOptional(matrizAtualizada.getCurso().id)
                    .orElseThrow(() -> new NotFoundException("Curso com ID " + matrizAtualizada.getCurso().id + " não encontrado."));
            matrizExistente.setCurso(novoCurso);
        }
        if (matrizAtualizada.getSemestre() != null && matrizAtualizada.getSemestre().id != null) {
            Semestre novoSemestre = semestreRepository.findByIdOptional(matrizAtualizada.getSemestre().id)
                    .orElseThrow(() -> new NotFoundException("Semestre com ID " + matrizAtualizada.getSemestre().id + " não encontrado."));
            matrizExistente.setSemestre(novoSemestre);
        }

        // Revalida unicidade caso curso ou semestre tenham sido alterados
        if (matrizCurricularRepository
                .find("curso = ?1 and semestre = ?2 and id <> ?3", matrizExistente.getCurso(), matrizExistente.getSemestre(), matrizExistente.id).count() > 0) {
            throw new BadRequestException("Já existe outra matriz curricular com o mesmo Curso e Semestre.");
        }

        // Atualiza a lista de disciplinas
        Set<Disciplina> disciplinasAtualizadas = new HashSet<>();
        if (matrizAtualizada.getDisciplinas() != null && !matrizAtualizada.getDisciplinas().isEmpty()) {
            for (Disciplina disciplina : matrizAtualizada.getDisciplinas()) {
                if (disciplina.id == null) {
                    throw new BadRequestException("IDs de disciplina devem ser fornecidos para associar à matriz.");
                }
                Disciplina disciplinaExistente = disciplinaRepository.findByIdOptional(disciplina.id)
                        .orElseThrow(() -> new NotFoundException("Disciplina com ID " + disciplina.id + " não encontrada."));
                disciplinasAtualizadas.add(disciplinaExistente);
            }
        }
        matrizExistente.setDisciplinas(disciplinasAtualizadas);

        return matrizExistente;
    }

    @Transactional
    public boolean deletarMatrizCurricular(Long id) {
        return matrizCurricularRepository.deleteById(id);
    }
}