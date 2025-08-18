package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class MatrizCurricularRequestDTO {
    @NotNull(message = "O ID do curso é obrigatório.")
    public Long cursoId;
    public List<PeriodoMatrizRequestDTO> periodos;

    // Construtores, Getters e Setters
    public MatrizCurricularRequestDTO() {}

    public MatrizCurricularRequestDTO(Long cursoId, List<PeriodoMatrizRequestDTO> periodos) {
        this.cursoId = cursoId;
        this.periodos = periodos;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public List<PeriodoMatrizRequestDTO> getPeriodos() {
        return periodos;
    }

    public void setPeriodos(List<PeriodoMatrizRequestDTO> periodos) {
        this.periodos = periodos;
    }
}
