package com.glaydson.controleacademico.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "aluno") // Nome da tabela no banco de dados
@DiscriminatorValue("ALUNO") // Valor para a coluna 'tipo_pessoa' na tabela Pessoa
public class Aluno extends Pessoa {

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false) // Chave estrangeira para Curso
    public Curso curso;

    public Aluno() {}

    public Aluno(String nome, String matricula, Curso curso) {
        super(nome, matricula);
        this.curso = curso;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}