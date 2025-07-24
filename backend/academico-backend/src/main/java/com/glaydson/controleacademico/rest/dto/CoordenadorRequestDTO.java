package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CoordenadorRequestDTO {

    @NotBlank(message = "O nome do coordenador é obrigatório.")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres.")
    public String nome;

    @NotBlank(message = "A matrícula do coordenador é obrigatória.")
    @Size(max = 20, message = "A matrícula não pode exceder 20 caracteres.")
    public String matricula;

    @NotNull(message = "O ID do curso é obrigatório.")
    public Long cursoId;

    // Construtores, Getters e Setters
    public CoordenadorRequestDTO() {}

    public CoordenadorRequestDTO(String nome, String matricula, Long cursoId) {
        this.nome = nome;
        this.matricula = matricula;
        this.cursoId = cursoId;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }
}