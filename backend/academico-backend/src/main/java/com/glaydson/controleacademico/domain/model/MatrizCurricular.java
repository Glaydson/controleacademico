package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "matriz_curricular",
        uniqueConstraints = @UniqueConstraint(columnNames = {"curso_id", "semestre_id"})) // Garante unicidade por Curso e Semestre
@SequenceGenerator(name = "matriz_curricular_seq",
        sequenceName = "matriz_curricular_seq", allocationSize = 1)
public class MatrizCurricular extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    public Curso curso;

    @ManyToOne
    @JoinColumn(name = "semestre_id", nullable = false)
    public Semestre semestre;

    @ManyToMany
    @JoinTable(
            name = "matriz_disciplina", // Tabela de junção para MatrizCurricular e Disciplina
            joinColumns = @JoinColumn(name = "matriz_id"),
            inverseJoinColumns = @JoinColumn(name = "disciplina_id")
    )
    public Set<Disciplina> disciplinas = new HashSet<>(); // Disciplinas oferecidas neste semestre para este curso

    public MatrizCurricular() {}

    public MatrizCurricular(Curso curso, Semestre semestre) {
        this.curso = curso;
        this.semestre = semestre;
    }

    // Getters e Setters
    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Semestre getSemestre() {
        return semestre;
    }

    public void setSemestre(Semestre semestre) {
        this.semestre = semestre;
    }

    public Set<Disciplina> getDisciplinas() {
        return disciplinas;
    }

    public void setDisciplinas(Set<Disciplina> disciplinas) {
        this.disciplinas = disciplinas;
    }

    public void addDisciplina(Disciplina disciplina) {
        this.disciplinas.add(disciplina);
    }

    public void removeDisciplina(Disciplina disciplina) {
        this.disciplinas.remove(disciplina);
    }
}