package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Semestre;
import com.glaydson.controleacademico.domain.repository.SemestreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    public Optional<Semestre> buscarSemestrePorNome(String nome) {
        return semestreRepository.find("nome", nome).firstResultOptional();
    }

    @Transactional
    public Semestre criarSemestre(Semestre semestre) {
        if (semestre.id != null) {
            throw new BadRequestException("ID deve ser nulo para criar um novo semestre.");
        }
        if (semestreRepository.find("nome", semestre.getNome()).count() > 0) {
            throw new BadRequestException("Já existe um semestre com o nome " + semestre.getNome());
        }
        semestreRepository.persist(semestre);
        return semestre;
    }

    @Transactional
    public Semestre atualizarSemestre(Long id, Semestre semestreAtualizado) {
        Semestre semestreExistente = semestreRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Semestre com ID " + id + " não encontrado."));

        if (!semestreExistente.getNome().equals(semestreAtualizado.getNome()) &&
                semestreRepository.find("nome", semestreAtualizado.getNome()).count() > 0) {
            throw new BadRequestException("Já existe outro semestre com o nome " + semestreAtualizado.getNome());
        }

        semestreExistente.setNome(semestreAtualizado.getNome());
        return semestreExistente;
    }

    @Transactional
    public boolean deletarSemestre(Long id) {
        // Adicione lógica para verificar se o semestre está em uso (ex: em Matrizes Curriculares)
        return semestreRepository.deleteById(id);
    }
}