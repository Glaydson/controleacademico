package com.glaydson.controleacademico.rest.dto;

import com.glaydson.controleacademico.domain.model.Disciplina;
import java.util.Set;
import java.util.stream.Collectors;

public class DisciplinaResponseDTO {
    public Long id;
    public String nome;
    public String codigo;
    public Set<CursoResponseDTO> cursos; // Compact DTOs for associated Cursos

    public DisciplinaResponseDTO() {}

    // Constructor to convert from Disciplina entity to DisciplinaResponseDTO
    public DisciplinaResponseDTO(Disciplina disciplina) {
        this.id = disciplina.id;
        this.nome = disciplina.nome;
        this.codigo = disciplina.codigo;
        if (disciplina.getCursos() != null) {
            this.cursos = disciplina.getCursos().stream()
                    .map(CursoResponseDTO::new)
                    .collect(Collectors.toSet());
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Set<CursoResponseDTO> getCursos() { return cursos; }
    public void setCursos(Set<CursoResponseDTO> cursos) { this.cursos = cursos; }
}