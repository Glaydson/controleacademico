package com.glaydson.controleacademico.domain.repository;

import com.glaydson.controleacademico.domain.model.Disciplina;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DisciplinaRepository implements PanacheRepository<Disciplina> {
}
