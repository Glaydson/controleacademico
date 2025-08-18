package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "matriz_curricular",
        uniqueConstraints = @UniqueConstraint(columnNames = {"curso_id"})) // Unicidade apenas por Curso
@SequenceGenerator(name = "matriz_curricular_seq",
        sequenceName = "matriz_curricular_seq", allocationSize = 1)
public class MatrizCurricular extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    public Curso curso;

    @OneToMany(mappedBy = "matrizCurricular", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PeriodoMatriz> periodos;

    public MatrizCurricular() {}

    public MatrizCurricular(Curso curso) {
        this.curso = curso;
    }

    // Getters e Setters
    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public List<PeriodoMatriz> getPeriodos() {
        return periodos;
    }

    public void setPeriodos(List<PeriodoMatriz> periodos) {
        this.periodos = periodos;
    }
}