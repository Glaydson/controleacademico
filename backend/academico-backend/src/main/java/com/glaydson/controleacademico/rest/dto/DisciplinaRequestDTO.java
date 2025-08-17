package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DisciplinaRequestDTO {

    @NotBlank(message = "O nome da disciplina é obrigatório.")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres.")
    public String nome;

    @NotBlank(message = "O código da disciplina é obrigatório.")
    @Size(max = 50, message = "O código não pode exceder 50 caracteres.")
    public String codigo;

    @NotNull(message = "O ID do curso é obrigatório.")
    public Long cursoId; // ID do curso ao qual a disciplina pertence

    public DisciplinaRequestDTO() {}

    public DisciplinaRequestDTO(String nome, String codigo, Long cursoId) {
        this.nome = nome;
        this.codigo = codigo;
        this.cursoId = cursoId;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }
}