package com.glaydson.controleacademico.domain.repository;

import com.glaydson.controleacademico.domain.model.Curso;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CursoRepository implements PanacheRepository<Curso> {
}
