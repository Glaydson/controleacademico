package com.glaydson.controleacademico.domain.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PROFESSOR") // Valor para a coluna 'tipo_pessoa'
public class Professor extends Pessoa {

    @Column(nullable = false, unique = true)
    public String registro; // Ex: SIAPE, número de registro interno

    public Professor() {}

    public Professor(String nome, String registro) {
        super(nome);
        this.registro = registro;
    }

    // Getters e Setters
    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }
}
