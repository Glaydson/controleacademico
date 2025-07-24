package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Semestre;
import com.glaydson.controleacademico.domain.repository.SemestreRepository; // Assumindo SemestreRepository existe
import com.glaydson.controleacademico.rest.dto.SemestreRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SemestreService {

    SemestreRepository semestreRepository;

    public SemestreService(SemestreRepository semestreRepository) {
        this.semestreRepository = semestreRepository;
    }

    public List<Semestre> listarTodosSemestres() {
        return semestreRepository.listAll();
    }

    public Optional<Semestre> buscarSemestrePorId(Long id) {
        return semestreRepository.findByIdOptional(id);
    }

    public Optional<Semestre> buscarSemestrePorAnoPeriodo(Integer ano, String periodo) {
        return semestreRepository.find("ano = ?1 and periodo = ?2", ano, periodo).firstResultOptional();
    }

    @Transactional
    public Semestre criarSemestre(SemestreRequestDTO semestreDto) { // Recebe DTO
        // Validação de unicidade para ano e período combinados
        if (semestreRepository.find("ano = ?1 and periodo = ?2", semestreDto.getAno(), semestreDto.getPeriodo()).count() > 0) {
            throw new BadRequestException("Já existe um semestre para o ano " + semestreDto.getAno() + " e período " + semestreDto.getPeriodo());
        }

        Semestre semestre = new Semestre();
        semestre.setAno(semestreDto.getAno());
        semestre.setPeriodo(semestreDto.getPeriodo());

        semestreRepository.persist(semestre);
        return semestre;
    }

    @Transactional
    public Semestre atualizarSemestre(Long id, SemestreRequestDTO semestreDto) { // Recebe DTO
        Semestre semestreExistente = semestreRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Semestre com ID " + id + " não encontrado."));

        // Validação de unicidade para ano e período combinados (se alterados)
        if (!semestreExistente.getAno().equals(semestreDto.getAno()) || !semestreExistente.getPeriodo().equals(semestreDto.getPeriodo())) {
            if (semestreRepository.find("ano = ?1 and periodo = ?2 and id <> ?3", semestreDto.getAno(), semestreDto.getPeriodo(), id).count() > 0) {
                throw new BadRequestException("Já existe outro semestre para o ano " + semestreDto.getAno() + " e período " + semestreDto.getPeriodo());
            }
        }

        semestreExistente.setAno(semestreDto.getAno());
        semestreExistente.setPeriodo(semestreDto.getPeriodo());

        return semestreExistente;
    }

    @Transactional
    public boolean deletarSemestre(Long id) {
        // Adicione lógica aqui para verificar se o semestre está em uso (ex: turmas, matrículas)
        // antes de permitir a deleção.
        return semestreRepository.deleteById(id);
    }
}