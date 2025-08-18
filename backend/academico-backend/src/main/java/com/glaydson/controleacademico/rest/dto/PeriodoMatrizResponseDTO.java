package com.glaydson.controleacademico.rest.dto;

import java.util.HashSet;
import java.util.Set;

public class PeriodoMatrizResponseDTO {
    public Integer numero;
    public Set<DisciplinaResponseDTO> disciplinas = new HashSet<>();

    public PeriodoMatrizResponseDTO() {}
    public PeriodoMatrizResponseDTO(Integer numero, Set<DisciplinaResponseDTO> disciplinas) {
        this.numero = numero;
        this.disciplinas = disciplinas;
    }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Set<DisciplinaResponseDTO> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<DisciplinaResponseDTO> disciplinas) { this.disciplinas = disciplinas; }
}

