package com.glaydson.controleacademico.rest.dto;

import com.glaydson.controleacademico.domain.model.Disciplina;

public class DisciplinaResponseDTO {
    public Long id;
    public String nome;
    public String codigo;
    public CursoResponseDTO curso; // Single curso instead of Set<CursoResponseDTO>
    public ProfessorResponseDTO professor;

    public DisciplinaResponseDTO() {}

    // Constructor to convert from Disciplina entity to DisciplinaResponseDTO
    public DisciplinaResponseDTO(Disciplina disciplina) {
        this.id = disciplina.id;
        this.nome = disciplina.nome;
        this.codigo = disciplina.codigo;
        if (disciplina.getCurso() != null) {
            // Use simplified constructor to avoid circular references and lazy loading issues
            this.curso = new CursoResponseDTO(disciplina.getCurso(), true);
        }
        if (disciplina.getProfessor() != null) {
            // Use o novo construtor para evitar referência circular
            this.professor = new ProfessorResponseDTO(disciplina.getProfessor(), false);
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public CursoResponseDTO getCurso() { return curso; }
    public void setCurso(CursoResponseDTO curso) { this.curso = curso; }
    public ProfessorResponseDTO getProfessor() { return professor; }
    public void setProfessor(ProfessorResponseDTO professor) { this.professor = professor; }
}