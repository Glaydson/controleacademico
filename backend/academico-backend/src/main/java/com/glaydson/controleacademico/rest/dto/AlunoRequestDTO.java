package com.glaydson.controleacademico.rest.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AlunoRequestDTO {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres.")
    public String nome;

    @NotBlank(message = "A matrícula é obrigatória.")
    @Pattern(regexp = "^[0-9]{1,10}$", message = "A matrícula deve conter apenas números e ter no máximo 10 dígitos.")
    public String matricula;

    @NotNull(message = "O ID do curso é obrigatório.")
    public Long cursoId;

    // Construtores, Getters e Setters (para Jackson/JAX-RS converterem o JSON)
    // Opcional: Você pode gerar isso com sua IDE ou Lombok

    public AlunoRequestDTO() {
    }

    public AlunoRequestDTO(String nome, String matricula, Long cursoId) {
        this.nome = nome;
        this.matricula = matricula;
        this.cursoId = cursoId;
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

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }
}
