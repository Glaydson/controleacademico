package com.glaydson.controleacademico.rest.dto;

import com.glaydson.controleacademico.domain.model.Curso;
import java.util.Set;
import java.util.stream.Collectors;

public class CursoResponseDTO {
    public Long id;
    public String nome;
    public String codigo;
    public CoordenadorResponseDTO coordenador;
    public Set<DisciplinaResponseDTO> disciplinas;

    public CursoResponseDTO() {}

    public CursoResponseDTO(Curso curso) {
        this.id = curso.id;
        this.nome = curso.nome;
        this.codigo = curso.codigo;

        if (curso.getCoordenador() != null) {
            this.coordenador = new CoordenadorResponseDTO(curso.getCoordenador());
        }

        // Only load disciplinas if explicitly requested (not when called from DisciplinaResponseDTO)
        // This prevents circular references and lazy loading issues
        if (curso.getDisciplinas() != null && !isCalledFromDisciplinaContext()) {
            this.disciplinas = curso.getDisciplinas().stream()
                    .map(disciplina -> {
                        // Create a simple DisciplinaResponseDTO without curso details to avoid recursion
                        DisciplinaResponseDTO dto = new DisciplinaResponseDTO();
                        dto.id = disciplina.id;
                        dto.nome = disciplina.nome;
                        dto.codigo = disciplina.codigo;
                        // Don't set curso to avoid circular reference
                        return dto;
                    })
                    .collect(Collectors.toSet());
        }
    }

    // Constructor for simplified curso info (used by DisciplinaResponseDTO)
    public CursoResponseDTO(Curso curso, boolean simplified) {
        this.id = curso.id;
        this.nome = curso.nome;
        this.codigo = curso.codigo;

        // Don't load coordenador or disciplinas for simplified version
        if (!simplified && curso.getCoordenador() != null) {
            this.coordenador = new CoordenadorResponseDTO(curso.getCoordenador());
        }
    }

    private boolean isCalledFromDisciplinaContext() {
        // Check if this constructor is being called from DisciplinaResponseDTO
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("DisciplinaResponseDTO")) {
                return true;
            }
        }
        return false;
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