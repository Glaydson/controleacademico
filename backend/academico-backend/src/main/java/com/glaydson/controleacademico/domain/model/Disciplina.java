package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Disciplina extends PanacheEntity {

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false, unique = true)
    public String codigo; // Ex: "COMP101", "MAT205"

    @ManyToMany // Uma disciplina pode ser ministrada em vários cursos, e um curso tem várias disciplinas
    @JoinTable(
            name = "disciplina_curso", // Nome da tabela de junção
            joinColumns = @JoinColumn(name = "disciplina_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    public Set<Curso> cursos = new HashSet<>(); // Lista dos cursos nas quais ela é ministrada

    public Disciplina() {}

    public Disciplina(String nome, String codigo) {
        this.nome = nome;
        this.codigo = codigo;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Set<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(Set<Curso> cursos) {
        this.cursos = cursos;
    }

    public void addCurso(Curso curso) {
        this.cursos.add(curso);
    }

    public void removeCurso(Curso curso) {
        this.cursos.remove(curso);
    }
}