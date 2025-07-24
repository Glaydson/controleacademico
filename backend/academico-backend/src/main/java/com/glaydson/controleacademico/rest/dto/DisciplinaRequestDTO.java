package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

public class DisciplinaRequestDTO {

    @NotBlank(message = "O nome da disciplina é obrigatório.")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres.")
    public String nome;

    @NotBlank(message = "O código da disciplina é obrigatório.")
    @Size(max = 50, message = "O código não pode exceder 50 caracteres.")
    public String codigo;

    public Set<Long> cursoIds = new HashSet<>(); // IDs dos cursos a serem associados

    public DisciplinaRequestDTO() {}

    public DisciplinaRequestDTO(String nome, String codigo, Set<Long> cursoIds) {
        this.nome = nome;
        this.codigo = codigo;
        this.cursoIds = cursoIds;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Set<Long> getCursoIds() { return cursoIds; }
    public void setCursoIds(Set<Long> cursoIds) { this.cursoIds = cursoIds; }
}