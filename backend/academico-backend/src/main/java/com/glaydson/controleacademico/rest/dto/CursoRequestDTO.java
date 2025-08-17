package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CursoRequestDTO {

    @NotBlank(message = "O nome do curso é obrigatório.")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres.")
    public String nome;

    @NotBlank(message = "O código do curso é obrigatório.")
    @Size(max = 50, message = "O código não pode exceder 50 caracteres.")
    public String codigo;

    public Long coordenadorId; // ID do coordenador (opcional)

    public CursoRequestDTO() {}

    public CursoRequestDTO(String nome, String codigo, Long coordenadorId) {
        this.nome = nome;
        this.codigo = codigo;
        this.coordenadorId = coordenadorId;
    }

    // Construtor sobrecarregado sem coordenador para flexibilidade
    public CursoRequestDTO(String nome, String codigo) {
        this.nome = nome;
        this.codigo = codigo;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Long getCoordenadorId() { return coordenadorId; }
    public void setCoordenadorId(Long coordenadorId) { this.coordenadorId = coordenadorId; }
}