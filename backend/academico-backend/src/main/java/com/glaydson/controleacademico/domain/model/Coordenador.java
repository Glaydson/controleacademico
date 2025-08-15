package com.glaydson.controleacademico.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coordenador", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"curso_id"}) // UM CURSO SÓ PODE TER UM COORDENADOR!
})
@DiscriminatorValue("COORDENADOR") // Valor para a coluna 'tipo_pessoa'
public class Coordenador extends Pessoa { // Agora Coordenador estende Pessoa

    @Column(nullable = false, unique = true)
    public String registro;

    @OneToOne // Relação 1:1 com Curso, esta é a entidade proprietária
    @JoinColumn(name = "curso_id", nullable = false, unique = true) // 'unique=true' garante o 1:1 no DB
    public Curso curso;

    // Construtores
    public Coordenador() {}

    public Coordenador(String nome, String registro, Curso curso, String keycloakId) { // Construtor com email
        super(nome, keycloakId); // Chama o construtor da superclasse Pessoa
        this.registro = registro;
        this.curso = curso;
    }

   // Getters e Setters
    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }

}
