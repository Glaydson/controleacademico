package com.glaydson.controleacademico.domain.repository;

import com.glaydson.controleacademico.domain.model.MatrizCurricular;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MatrizCurricularRepository implements PanacheRepository<MatrizCurricular> {
}
