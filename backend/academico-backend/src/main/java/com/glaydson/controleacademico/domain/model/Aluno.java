package com.glaydson.controleacademico.domain.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ALUNO") // Valor para a coluna 'tipo_pessoa' na tabela Pessoa
public class Aluno extends Pessoa {

    @Column(nullable = false, unique = true)
    public String matricula;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false) // Chave estrangeira para Curso
    public Curso curso;

    public Aluno() {}

    public Aluno(String nome, String matricula, Curso curso) {
        super(nome);
        this.matricula = matricula;
        this.curso = curso;
    }

    // Getters e Setters
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}