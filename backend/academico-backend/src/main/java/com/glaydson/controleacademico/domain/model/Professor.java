package com.glaydson.controleacademico.domain.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "professor") // Tabela específica para Professor
@DiscriminatorValue("PROFESSOR") // Valor para a coluna 'tipo_pessoa'
public class Professor extends Pessoa { // Agora Professor estende Pessoa

    @Column(nullable = false, unique = true)
    private String registro;

    @OneToMany(mappedBy = "professor", fetch = FetchType.LAZY)
    public Set<Disciplina> disciplinas = new HashSet<>();

    // Construtores
    public Professor() {}

    public Professor(String nome, String registro, Set<Disciplina> disciplinas, String keycloakId) { // Construtor com email
        super(nome, keycloakId); // Chama o construtor da superclasse Pessoa
        this.registro = registro;
        this.disciplinas = disciplinas;
    }

    // Getters e Setters (para o campo específico de Professor)
    public Set<Disciplina> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<Disciplina> disciplinas) { this.disciplinas = disciplinas; }

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }
}
