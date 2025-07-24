package com.glaydson.controleacademico.domain.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "professor") // Tabela específica para Professor
@DiscriminatorValue("PROFESSOR") // Valor para a coluna 'tipo_pessoa'
public class Professor extends Pessoa { // Agora Professor estende Pessoa

    @ManyToMany(cascade = {jakarta.persistence.CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "professor_disciplina",
            joinColumns = @jakarta.persistence.JoinColumn(name = "professor_id"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "disciplina_id"))
    public Set<Disciplina> disciplinas = new HashSet<>();

    // Construtores
    public Professor() {}

    public Professor(String nome, String matricula, Set<Disciplina> disciplinas) { // Construtor com email
        super(nome, matricula); // Chama o construtor da superclasse Pessoa
        this.disciplinas = disciplinas;
    }

    // Getters e Setters (para o campo específico de Professor)
    public Set<Disciplina> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<Disciplina> disciplinas) { this.disciplinas = disciplinas; }


}
