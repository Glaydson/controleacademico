package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
public class Semestre extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String nome; // Ex: "2023.1", "2024.2", "Primeiro Semestre"

    public Semestre() {}

    public Semestre(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}