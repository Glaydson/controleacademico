package com.glaydson.controleacademico.domain.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("COORDENADOR") // Valor para a coluna 'tipo_pessoa'
public class Coordenador extends Pessoa {

    @Column(nullable = false, unique = true)
    public String registro; // Registro similar ao professor, mas talvez de coordenação

    public Coordenador() {}

    public Coordenador(String nome, String registro) {
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
