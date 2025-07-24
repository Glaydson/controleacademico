package com.glaydson.controleacademico.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "curso", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nome"}) // Nome do curso deve ser único
})
@SequenceGenerator(name = "curso_seq", sequenceName = "curso_seq", allocationSize = 1)
public class Curso extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "curso_seq")
    public Long id;

    @Column(nullable = false, unique = true)
    public String nome;

    @Column(nullable = false, unique = true)
    public String codigo;

    // Relacionamento Many-to-Many com Disciplina
    // mappedBy indica que a Disciplina é o lado "proprietário" do relacionamento
    // cascade = CascadeType.MERGE para que ao salvar um Curso, as Disciplinas relacionadas sejam mescladas se existirem.
    // fetch = FetchType.LAZY para carregar disciplinas apenas quando necessário.
    @ManyToMany(mappedBy = "cursos", cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    public Set<Disciplina> disciplinas = new HashSet<>(); // Conjunto de disciplinas associadas a este curso

    @OneToOne
    public Coordenador coordenador;

    // Construtores
    public Curso() {
    }

    public Curso(String nome, String codigo) {
        this.nome = nome;
        this.codigo = codigo;
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Set<Disciplina> getDisciplinas() {
        return disciplinas;
    }

    public void setDisciplinas(Set<Disciplina> disciplinas) {
        this.disciplinas = disciplinas;
    }

    public Coordenador getCoordenador() {
        return coordenador;
    }

    public void setCoordenador(Coordenador coordenador) {
        this.coordenador = coordenador;
    }

    // Métodos utilitários para adicionar/remover disciplinas
    public void addDisciplina(Disciplina disciplina) {
        this.disciplinas.add(disciplina);
        disciplina.getCursos().add(this);
    }

    public void removeDisciplina(Disciplina disciplina) {
        this.disciplinas.remove(disciplina);
        disciplina.getCursos().remove(this);
    }
}