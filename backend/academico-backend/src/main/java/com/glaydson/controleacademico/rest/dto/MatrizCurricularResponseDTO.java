package com.glaydson.controleacademico.rest.dto;

import com.glaydson.controleacademico.domain.model.MatrizCurricular;

import java.util.List;
import java.util.stream.Collectors;

public class MatrizCurricularResponseDTO {
    public Long id;
    public Long cursoId;
    public String cursoNome;
    public List<PeriodoMatrizResponseDTO> periodos;

    public MatrizCurricularResponseDTO(MatrizCurricular matrizCurricular) {
        this.id = matrizCurricular.id;
        if (matrizCurricular.getCurso() != null) {
            this.cursoId = matrizCurricular.getCurso().id;
            this.cursoNome = matrizCurricular.getCurso().nome;
        }
        if (matrizCurricular.getPeriodos() != null) {
            this.periodos = matrizCurricular.getPeriodos().stream()
                    .map(periodo -> new PeriodoMatrizResponseDTO(
                            periodo.getNumero(),
                            periodo.getDisciplinas().stream().map(DisciplinaResponseDTO::new).collect(Collectors.toSet())
                    ))
                    .collect(Collectors.toList());
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }
    public String getCursoNome() { return cursoNome; }
    public void setCursoNome(String cursoNome) { this.cursoNome = cursoNome; }
    public List<PeriodoMatrizResponseDTO> getPeriodos() { return periodos; }
    public void setPeriodos(List<PeriodoMatrizResponseDTO> periodos) { this.periodos = periodos; }
}