package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Disciplina;
import com.glaydson.controleacademico.domain.model.MatrizCurricular;
import com.glaydson.controleacademico.domain.model.PeriodoMatriz;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.domain.repository.MatrizCurricularRepository;
import com.glaydson.controleacademico.rest.dto.MatrizCurricularRequestDTO;
import com.glaydson.controleacademico.rest.dto.PeriodoMatrizRequestDTO;
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
    DisciplinaRepository disciplinaRepository;

    public MatrizCurricularService(MatrizCurricularRepository matrizCurricularRepository,
                                   CursoRepository cursoRepository, DisciplinaRepository disciplinaRepository) {
        this.matrizCurricularRepository = matrizCurricularRepository;
        this.cursoRepository = cursoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    public List<MatrizCurricular> listarTodasMatrizesCurriculares() {
        return matrizCurricularRepository.listAll();
    }

    public Optional<MatrizCurricular> buscarMatrizCurricularPorId(Long id) {
        return matrizCurricularRepository.findByIdOptional(id);
    }

    private void validarDisciplinasMatriz(Long cursoId, List<PeriodoMatrizRequestDTO> periodos) {
        java.util.Set<Long> allDisciplinaIds = new java.util.HashSet<>();
        java.util.Set<Long> duplicateDisciplinaIds = new java.util.HashSet<>();
        for (PeriodoMatrizRequestDTO periodo : periodos) {
            for (Long disciplinaId : periodo.getDisciplinaIds()) {
                if (!allDisciplinaIds.add(disciplinaId)) {
                    duplicateDisciplinaIds.add(disciplinaId);
                }
                Disciplina disciplina = disciplinaRepository.findById(disciplinaId);
                if (disciplina == null) {
                    throw new NotFoundException("Disciplina não encontrada: " + disciplinaId);
                }
                if (disciplina.curso == null || disciplina.curso.id == null || !disciplina.curso.id.equals(cursoId)) {
                    throw new BadRequestException("Disciplina '" + disciplina.nome + "' (ID: " + disciplinaId + ") não pertence ao curso informado.");
                }
            }
        }
        System.out.println("DEBUG: allDisciplinaIds=" + allDisciplinaIds + ", duplicateDisciplinaIds=" + duplicateDisciplinaIds);
        if (!duplicateDisciplinaIds.isEmpty()) {
            System.out.println("THROWING BadRequestException for duplicates: " + duplicateDisciplinaIds);
            throw new BadRequestException("Disciplina(s) presente(s) em mais de um período: " + duplicateDisciplinaIds);
        }
    }

    @Transactional
    public MatrizCurricular criarMatrizCurricular(MatrizCurricularRequestDTO matrizDto) {
        if (matrizDto.getCursoId() == null) {
            throw new BadRequestException("O ID do curso é obrigatório.");
        }
        Curso curso = cursoRepository.findById(matrizDto.getCursoId());
        if (curso == null) {
            throw new NotFoundException("Curso não encontrado.");
        }
        if (matrizDto.getPeriodos() != null) {
            validarDisciplinasMatriz(matrizDto.getCursoId(), matrizDto.getPeriodos());
        }
        MatrizCurricular matriz = new MatrizCurricular(curso);
        matriz.periodos = new java.util.ArrayList<>();
        if (matrizDto.getPeriodos() != null) {
            for (PeriodoMatrizRequestDTO periodoDto : matrizDto.getPeriodos()) {
                PeriodoMatriz periodo = new PeriodoMatriz();
                periodo.numero = periodoDto.getNumero();
                periodo.matrizCurricular = matriz;
                periodo.disciplinas = new HashSet<>();
                if (periodoDto.getDisciplinaIds() != null) {
                    for (Long disciplinaId : periodoDto.getDisciplinaIds()) {
                        Disciplina disciplina = disciplinaRepository.findById(disciplinaId);
                        periodo.disciplinas.add(disciplina);
                    }
                }
                matriz.periodos.add(periodo);
            }
        }
        matrizCurricularRepository.persist(matriz);
        return matriz;
    }

    @Transactional
    public MatrizCurricular atualizarMatrizCurricular(Long id, MatrizCurricularRequestDTO matrizDto) {
        MatrizCurricular matrizExistente = matrizCurricularRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Matriz Curricular com ID " + id + " não encontrada."));

        // Atualiza curso se fornecido (e valida)
        if (matrizDto.cursoId != null) {
            Curso novoCurso = cursoRepository.findByIdOptional(matrizDto.cursoId)
                    .orElseThrow(() -> new NotFoundException("Curso com ID " + matrizDto.cursoId + " não encontrado."));
            matrizExistente.setCurso(novoCurso);
        }

        // Revalida unicidade caso curso tenha sido alterado
        if (matrizCurricularRepository.find("curso = ?1 and id <> ?2", matrizExistente.getCurso(), matrizExistente.id).count() > 0) {
            throw new BadRequestException("Já existe outra matriz curricular com o mesmo Curso.");
        }

        if (matrizDto.getPeriodos() != null) {
            validarDisciplinasMatriz(matrizExistente.getCurso().id, matrizDto.getPeriodos());
            List<PeriodoMatriz> periodosAtualizados = new java.util.ArrayList<>();
            for (PeriodoMatrizRequestDTO periodoDto : matrizDto.getPeriodos()) {
                PeriodoMatriz periodo = new PeriodoMatriz();
                periodo.setNumero(periodoDto.getNumero());
                periodo.setMatrizCurricular(matrizExistente);
                java.util.Set<Disciplina> disciplinasAtualizadas = new java.util.HashSet<>();
                if (periodoDto.getDisciplinaIds() != null) {
                    for (Long disciplinaId : periodoDto.getDisciplinaIds()) {
                        Disciplina disciplinaExistente = disciplinaRepository.findByIdOptional(disciplinaId)
                                .orElseThrow(() -> new NotFoundException("Disciplina com ID " + disciplinaId + " não encontrada."));
                        disciplinasAtualizadas.add(disciplinaExistente);
                    }
                }
                periodo.setDisciplinas(disciplinasAtualizadas);
                periodosAtualizados.add(periodo);
            }
            matrizExistente.getPeriodos().clear();
            matrizExistente.getPeriodos().addAll(periodosAtualizados);
        }

        return matrizExistente;
    }

    @Transactional
    public boolean deletarMatrizCurricular(Long id) {
        return matrizCurricularRepository.deleteById(id);
    }
}