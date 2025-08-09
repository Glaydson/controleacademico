package com.glaydson.controleacademico.rest.dto;

import java.util.Set;

public class UserCreateRequestDTO {

    private String nome;
    private String email;
    private String password;
    private String matricula;
    private String role; // Expected values: "ALUNO", "PROFESSOR", "COORDENADOR"

    // Fields for specific roles
    private Long cursoId; // For ALUNO and COORDENADOR
    private Set<Long> disciplinaIds; // For PROFESSOR

    // Getters and Setters

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public Set<Long> getDisciplinaIds() {
        return disciplinaIds;
    }

    public void setDisciplinaIds(Set<Long> disciplinaIds) {
        this.disciplinaIds = disciplinaIds;
    }
}

