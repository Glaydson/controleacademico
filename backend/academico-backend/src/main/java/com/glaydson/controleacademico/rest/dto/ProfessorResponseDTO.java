package com.glaydson.controleacademico.rest.dto;

import com.glaydson.controleacademico.domain.model.Professor;

import java.util.Set;
import java.util.stream.Collectors;

public class ProfessorResponseDTO {
    public Long id;
    public String nome;
    public String email; // Email de volta!
    public String matricula;
    public Set<DisciplinaResponseDTO> disciplinas;

    // Construtor para converter da entidade Professor para o DTO
    public ProfessorResponseDTO(Professor professor) {
        this.id = professor.id;
        this.nome = professor.nome;
        this.matricula = professor.getRegistro();
        if (professor.disciplinas != null) {
            this.disciplinas = professor.disciplinas.stream()
                    .map(DisciplinaResponseDTO::new)
                    .collect(Collectors.toSet());
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public Set<DisciplinaResponseDTO> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<DisciplinaResponseDTO> disciplinas) { this.disciplinas = disciplinas; }
}