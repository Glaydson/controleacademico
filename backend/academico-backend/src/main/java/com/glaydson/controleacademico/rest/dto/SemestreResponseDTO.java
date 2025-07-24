package com.glaydson.controleacademico.rest.dto;

import com.glaydson.controleacademico.domain.model.Semestre; // Assumindo que a entidade Semestre existe

public class SemestreResponseDTO {
    public Long id;
    public Integer ano;
    public String periodo;
    public String descricao; // Campo derivado para exibição (ex: "2024.1")

    public SemestreResponseDTO() {}

    // Construtor para converter da entidade Semestre para o DTO
    public SemestreResponseDTO(Semestre semestre) {
        this.id = semestre.id;
        this.ano = semestre.ano;
        this.periodo = semestre.periodo;
        this.descricao = semestre.ano + "." + semestre.periodo; // Exemplo de campo derivado
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
