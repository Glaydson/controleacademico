package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class PeriodoMatrizRequestDTO {
    @NotNull(message = "O número do período é obrigatório.")
    public Integer numero;
    public Set<Long> disciplinaIds = new HashSet<>();

    public PeriodoMatrizRequestDTO() {}
    public PeriodoMatrizRequestDTO(Integer numero, Set<Long> disciplinaIds) {
        this.numero = numero;
        this.disciplinaIds = disciplinaIds;
    }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Set<Long> getDisciplinaIds() { return disciplinaIds; }
    public void setDisciplinaIds(Set<Long> disciplinaIds) { this.disciplinaIds = disciplinaIds; }
}

