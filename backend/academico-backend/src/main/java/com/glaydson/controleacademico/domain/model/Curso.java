package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
public class Curso extends PanacheEntity { // PanacheEntity já tem ID e métodos CRUD

    @Column(nullable = false, unique = true)
    public String nome;

    @Column(nullable = false, unique = true)
    public String codigo;

    public Curso() {}

    public Curso(String nome, String codigo) {
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
}