package com.glaydson.controleacademico.rest.dto;

import java.util.Set;

public class UserResponseDTO {

    private String id; // Keycloak user ID
    private String nome;
    private String email;
    private String matricula;
    private String role;
    private boolean enabled;
    
    // Fields for specific roles
    private Long cursoId; // For ALUNO and COORDENADOR
    private String cursoNome; // For display purposes
    private Set<Long> disciplinaIds; // For PROFESSOR
    private Set<String> disciplinaNomes; // For display purposes

    // Constructors
    public UserResponseDTO() {}

    public UserResponseDTO(String id, String nome, String email, String matricula, String role, boolean enabled) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.role = role;
        this.enabled = enabled;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public String getCursoNome() {
        return cursoNome;
    }

    public void setCursoNome(String cursoNome) {
        this.cursoNome = cursoNome;
    }

    public Set<Long> getDisciplinaIds() {
        return disciplinaIds;
    }

    public void setDisciplinaIds(Set<Long> disciplinaIds) {
        this.disciplinaIds = disciplinaIds;
    }

    public Set<String> getDisciplinaNomes() {
        return disciplinaNomes;
    }

    public void setDisciplinaNomes(Set<String> disciplinaNomes) {
        this.disciplinaNomes = disciplinaNomes;
    }
}
