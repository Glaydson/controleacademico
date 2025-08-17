package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "disciplina", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo"}) // Código da disciplina deve ser único
})
@SequenceGenerator(name = "disciplina_seq", sequenceName = "disciplina_seq", allocationSize = 1)
public class Disciplina extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "disciplina_seq")
    public Long id;

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false, unique = true)
    public String codigo; // Ex: "COMP201"

    // Relacionamento Many-to-One com Curso
    // Cada disciplina pertence a apenas um curso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    public Curso curso; // Curso ao qual esta disciplina pertence

    // Construtores
    public Disciplina() {
    }

    public Disciplina(String nome, String codigo, Curso curso) {
        this.nome = nome;
        this.codigo = codigo;
        this.curso = curso;
    }

    // Overloaded constructor without curso for flexibility
    public Disciplina(String nome, String codigo) {
        this.nome = nome;
        this.codigo = codigo;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}
