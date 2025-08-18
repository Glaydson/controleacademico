package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "periodo_matriz")
public class PeriodoMatriz extends PanacheEntity {

    @Column(nullable = false)
    public int numero; // Period number (1, 2, ... n)

    @ManyToOne
    @JoinColumn(name = "matriz_id", nullable = false)
    public MatrizCurricular matrizCurricular;

    @ManyToMany
    @JoinTable(
        name = "periodo_disciplina",
        joinColumns = @JoinColumn(name = "periodo_id"),
        inverseJoinColumns = @JoinColumn(name = "disciplina_id")
    )
    public Set<Disciplina> disciplinas = new HashSet<>();

    public PeriodoMatriz() {}

    public PeriodoMatriz(int numero, MatrizCurricular matrizCurricular) {
        this.numero = numero;
        this.matrizCurricular = matrizCurricular;
    }

    // Getters and Setters
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public MatrizCurricular getMatrizCurricular() { return matrizCurricular; }
    public void setMatrizCurricular(MatrizCurricular matrizCurricular) { this.matrizCurricular = matrizCurricular; }
    public Set<Disciplina> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<Disciplina> disciplinas) { this.disciplinas = disciplinas; }
}

