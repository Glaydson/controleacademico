package com.glaydson.controleacademico.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SemestreRequestDTO {

    @NotNull(message = "O ano do semestre é obrigatório.")
    @Min(value = 2000, message = "O ano deve ser igual ou superior a 2000.")
    public Integer ano;

    @NotBlank(message = "O período do semestre é obrigatório.")
    @Size(min = 1, max = 10, message = "O período deve ter entre 1 e 10 caracteres.")
    @Pattern(regexp = "^[1-2]$", message = "O período deve ser '1' ou '2'.") // Ex: "1" para primeiro semestre, "2" para segundo
    public String periodo;

    public SemestreRequestDTO() {}

    public SemestreRequestDTO(Integer ano, String periodo) {
        this.ano = ano;
        this.periodo = periodo;
    }

    // Getters e Setters
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
}