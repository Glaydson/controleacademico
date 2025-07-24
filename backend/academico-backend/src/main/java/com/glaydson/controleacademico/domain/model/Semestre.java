package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "semestre", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ano", "periodo"}) // Ano e período juntos devem ser únicos
})
@SequenceGenerator(name = "semestre_seq", sequenceName = "semestre_seq", allocationSize = 1)
public class Semestre extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "semestre_seq")
    public Long id;

    @Column(nullable = false)
    public Integer ano;

    @Column(nullable = false, length = 10) // Ex: "1", "2"
    public String periodo;

    // Construtores
    public Semestre() {}

    public Semestre(Integer ano, String periodo) {
        this.ano = ano;
        this.periodo = periodo;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    // Opcional: toString, equals e hashCode para melhor debug e uso em Sets
    @Override
    public String toString() {
        return "Semestre{" +
                "id=" + id +
                ", ano=" + ano +
                ", periodo='" + periodo + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Semestre semestre = (Semestre) o;
        return id != null && id.equals(semestre.id);
    }

    @Override
    public int hashCode() {
        return 31; // Ou Objects.hash(id); se id for sempre não nulo após persistência
    }
}