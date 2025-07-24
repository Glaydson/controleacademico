package com.glaydson.controleacademico.rest.dto;

import com.glaydson.controleacademico.domain.model.Curso;
import com.glaydson.controleacademico.domain.model.Coordenador; // Assuming Coordenador entity exists
import java.util.Set;
import java.util.stream.Collectors;

public class CursoResponseDTO {
    public Long id;
    public String nome;
    public String codigo;
    public CoordenadorResponseDTO coordenador;
    public Set<DisciplinaResponseDTO> disciplinas;

    public CursoResponseDTO() {}

    // Constructor to convert from Curso entity to CursoResponseDTO
    public CursoResponseDTO(Curso curso) {
        this.id = curso.id;
        this.nome = curso.nome;
        this.codigo = curso.codigo;

        if (curso.getCoordenador() != null) {
            this.coordenador = new CoordenadorResponseDTO(curso.getCoordenador());
        }

        if (curso.getCoordenador() != null) {
            this.coordenador = new CoordenadorResponseDTO(curso.getCoordenador());
        }


        if (curso.getDisciplinas() != null) {
            this.disciplinas = curso.getDisciplinas().stream()
                    .map(DisciplinaResponseDTO::new)
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
    public CoordenadorResponseDTO getCoordenador() { return coordenador; }
    public void setCoordenador(CoordenadorResponseDTO coordenador) { this.coordenador = coordenador; }
    public Set<DisciplinaResponseDTO> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<DisciplinaResponseDTO> disciplinas) { this.disciplinas = disciplinas; }
}