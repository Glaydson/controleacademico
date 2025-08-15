package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "pessoa") // Removed the invalid unique constraint on matricula
@Inheritance(strategy = InheritanceType.JOINED) // Estratégia de herança JOINED
@DiscriminatorColumn(name = "tipo_pessoa", discriminatorType = DiscriminatorType.STRING) // Coluna para identificar o tipo
@SequenceGenerator(name = "pessoa_seq", sequenceName = "pessoa_seq", allocationSize = 1)
public abstract class Pessoa extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pessoa_seq")
    public Long id;

    @Column(nullable = false)
    public String nome;

    @Column(name = "keycloak_id", unique = true)
    public String keycloakId;


    // Construtores, getters e setters
    public Pessoa() {}

    public Pessoa(String nome, String keycloakId) {
        this.nome = nome;
        this.keycloakId = keycloakId;
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

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }
}
