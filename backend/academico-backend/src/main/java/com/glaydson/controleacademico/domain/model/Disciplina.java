package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    // Relacionamento Many-to-Many com Curso
    // @JoinTable define a tabela intermediária e as colunas de união
    // joinColumns é a coluna desta entidade (Disciplina) na tabela intermediária
    // inverseJoinColumns é a coluna da entidade "inversa" (Curso) na tabela intermediária
    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "curso_disciplina",
            joinColumns = @JoinColumn(name = "disciplina_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id"))
    public Set<Curso> cursos = new HashSet<>(); // Conjunto de cursos aos quais esta disciplina pertence

    // Construtores
    public Disciplina() {
    }

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

    public Set<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(Set<Curso> cursos) {
        this.cursos = cursos;
    }

    // Métodos utilitários para adicionar/remover cursos
    public void addCurso(Curso curso) {
        this.cursos.add(curso);
        curso.getDisciplinas().add(this);
    }

    public void removeCurso(Curso curso) {
        this.cursos.remove(curso);
        curso.getDisciplinas().remove(this);
    }
}
