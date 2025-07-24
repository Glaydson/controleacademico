package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

public class CursoRequestDTO {

    @NotBlank(message = "O nome do curso é obrigatório.")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres.")
    public String nome;

    @NotBlank(message = "O código do curso é obrigatório.")
    @Size(max = 50, message = "O código não pode exceder 50 caracteres.")
    public String codigo;

    public Set<Long> disciplinaIds = new HashSet<>(); // IDs das disciplinas a serem associadas
    public Long coordenadorId; // ID do coordenador (se a entidade Curso tiver este relacionamento)

    public CursoRequestDTO() {}

    public CursoRequestDTO(String nome, String codigo, Set<Long> disciplinaIds, Long coordenadorId) {
        this.nome = nome;
        this.codigo = codigo;
        this.disciplinaIds = disciplinaIds;
        this.coordenadorId = coordenadorId;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Set<Long> getDisciplinaIds() { return disciplinaIds; }
    public void setDisciplinaIds(Set<Long> disciplinaIds) { this.disciplinaIds = disciplinaIds; }
    public Long getCoordenadorId() { return coordenadorId; }
    public void setCoordenadorId(Long coordenadorId) { this.coordenadorId = coordenadorId; }
}