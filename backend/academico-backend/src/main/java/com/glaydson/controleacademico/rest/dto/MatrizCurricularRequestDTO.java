package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MatrizCurricularRequestDTO {
    @NotNull(message = "O ID do curso é obrigatório.")
    public Long cursoId;

    @NotNull(message = "O ID do semestre é obrigatório.")
    public Long semestreId;

    public Set<Long> disciplinaIds = new HashSet<>(); // Conjunto de IDs de disciplinas

    // Construtores, Getters e Setters
    public MatrizCurricularRequestDTO() {}

    public MatrizCurricularRequestDTO(Long cursoId, Long semestreId, Set<Long> disciplinaIds) {
        this.cursoId = cursoId;
        this.semestreId = semestreId;
        this.disciplinaIds = disciplinaIds;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public Long getSemestreId() {
        return semestreId;
    }

    public void setSemestreId(Long semestreId) {
        this.semestreId = semestreId;
    }

    public Set<Long> getDisciplinaIds() {
        return disciplinaIds;
    }

    public void setDisciplinaIds(Set<Long> disciplinaIds) {
        this.disciplinaIds = disciplinaIds;
    }
}
