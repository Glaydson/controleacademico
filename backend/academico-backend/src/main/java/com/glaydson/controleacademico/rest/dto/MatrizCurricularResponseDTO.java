package com.glaydson.controleacademico.rest.dto;

import com.glaydson.controleacademico.domain.model.MatrizCurricular;

import java.util.Set;
import java.util.stream.Collectors;

public class MatrizCurricularResponseDTO {
    public Long id;
    public Long cursoId;
    public String cursoNome;
    public Long semestreId;
    public String semestreNome;
    public Set<DisciplinaResponseDTO> disciplinas; // Usando um DTO para as disciplinas

    // Construtor para converter da entidade MatrizCurricular para o DTO
    public MatrizCurricularResponseDTO(MatrizCurricular matrizCurricular) {
        this.id = matrizCurricular.id;
        if (matrizCurricular.getCurso() != null) {
            this.cursoId = matrizCurricular.getCurso().id;
            this.cursoNome = matrizCurricular.getCurso().nome;
        }
        if (matrizCurricular.getSemestre() != null) {
            this.semestreId = matrizCurricular.getSemestre().id;
            this.semestreNome = matrizCurricular.getSemestre().periodo;
        }
        if (matrizCurricular.getDisciplinas() != null) {
            this.disciplinas = matrizCurricular.getDisciplinas().stream()
                    .map(DisciplinaResponseDTO::new)
                    .collect(Collectors.toSet());
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }
    public String getCursoNome() { return cursoNome; }
    public void setCursoNome(String cursoNome) { this.cursoNome = cursoNome; }
    public Long getSemestreId() { return semestreId; }
    public void setSemestreId(Long semestreId) { this.semestreId = semestreId; }
    public String getSemestreNome() { return semestreNome; }
    public void setSemestreNome(String semestreNome) { this.semestreNome = semestreNome; }
    public Set<DisciplinaResponseDTO> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<DisciplinaResponseDTO> disciplinas) { this.disciplinas = disciplinas; }
}