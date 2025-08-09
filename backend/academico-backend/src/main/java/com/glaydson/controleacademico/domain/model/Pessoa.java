package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "pessoa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"matricula"}) // Matrícula deve ser única para todas as Pessoas
})
@Inheritance(strategy = InheritanceType.JOINED) // Estratégia de herança JOINED
@DiscriminatorColumn(name = "tipo_pessoa", discriminatorType = DiscriminatorType.STRING) // Coluna para identificar o tipo
@SequenceGenerator(name = "pessoa_seq", sequenceName = "pessoa_seq", allocationSize = 1)
public abstract class Pessoa extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pessoa_seq")
    public Long id;

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false, unique = true)
    public String matricula;

    @Column(name = "keycloak_id", unique = true)
    public String keycloakId;


    // Construtores, getters e setters
    public Pessoa() {}

    public Pessoa(String nome, String matricula) {
        this.nome = nome;
        this.matricula = matricula;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }
}
