package com.glaydson.controleacademico.domain.repository;

import com.glaydson.controleacademico.domain.model.Aluno;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlunoRepository implements PanacheRepository<Aluno> {
    // Métodos de consulta customizados, se precisar
}