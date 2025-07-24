package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

public class ProfessorRequestDTO {

    @NotBlank(message = "O nome do professor é obrigatório.")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres.")
    public String nome;

    @NotBlank(message = "A matrícula do professor é obrigatória.")
    @Size(max = 20, message = "A matrícula não pode exceder 20 caracteres.")
    public String matricula;

    public Set<Long> disciplinaIds = new HashSet<>();

    // Construtores, Getters e Setters
    public ProfessorRequestDTO() {}

    public ProfessorRequestDTO(String nome, String matricula, Set<Long> disciplinaIds) {
        this.nome = nome;
        this.matricula = matricula;
        this.disciplinaIds = disciplinaIds;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public Set<Long> getDisciplinaIds() { return disciplinaIds; }
    public void setDisciplinaIds(Set<Long> disciplinaIds) { this.disciplinaIds = disciplinaIds; }
}
