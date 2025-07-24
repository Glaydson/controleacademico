package com.glaydson.controleacademico.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coordenador", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"curso_id"}) // UM CURSO SÓ PODE TER UM COORDENADOR!
})
@DiscriminatorValue("COORDENADOR") // Valor para a coluna 'tipo_pessoa'
public class Coordenador extends Pessoa { // Agora Coordenador estende Pessoa

    @OneToOne // Relação 1:1 com Curso, esta é a entidade proprietária
    @JoinColumn(name = "curso_id", nullable = false, unique = true) // 'unique=true' garante o 1:1 no DB
    public Curso curso;

    // Construtores
    public Coordenador() {}

    public Coordenador(String nome, String matricula, Curso curso) { // Construtor com email
        super(nome, matricula); // Chama o construtor da superclasse Pessoa
        this.curso = curso;
    }

   // Getters e Setters
    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}
