package com.glaydson.controleacademico.rest.dto;


import com.glaydson.controleacademico.domain.model.Coordenador;

public class CoordenadorResponseDTO {
    public Long id;
    public String nome;
    public String matricula;
    public Long cursoId;
    public String cursoNome;

    // Construtor para converter da entidade Coordenador para o DTO
    public CoordenadorResponseDTO(Coordenador coordenador) {
        this.id = coordenador.id;
        this.nome = coordenador.nome;
        this.matricula = coordenador.getRegistro();
        if (coordenador.getCurso() != null) {
            this.cursoId = coordenador.getCurso().id;
            this.cursoNome = coordenador.getCurso().nome;
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }
    public String getCursoNome() { return cursoNome; }
    public void setCursoNome(String cursoNome) { this.cursoNome = cursoNome; }
}