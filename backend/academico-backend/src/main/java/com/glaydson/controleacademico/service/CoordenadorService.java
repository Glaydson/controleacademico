package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Coordenador;
import com.glaydson.controleacademico.domain.repository.CoordenadorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CoordenadorService {

    CoordenadorRepository coordenadorRepository;

    public CoordenadorService(CoordenadorRepository coordenadorRepository) {
        this.coordenadorRepository = coordenadorRepository;
    }

    public List<Coordenador> listarTodosCoordenadores() {
        return coordenadorRepository.listAll();
    }

    public Optional<Coordenador> buscarCoordenadorPorId(Long id) {
        return coordenadorRepository.findByIdOptional(id);
    }

    public Optional<Coordenador> buscarCoordenadorPorRegistro(String registro) {
        return coordenadorRepository.find("registro", registro).firstResultOptional();
    }

    @Transactional
    public Coordenador criarCoordenador(Coordenador coordenador) {
        if (coordenador.getId() != null) {
            throw new BadRequestException("ID deve ser nulo para criar um novo coordenador.");
        }
        // Exemplo de validação de negócio: garantir que o registro seja único
        if (coordenadorRepository.find("registro", coordenador.getRegistro()).count() > 0) {
            throw new BadRequestException("Já existe um coordenador com o registro " + coordenador.getRegistro());
        }
        coordenadorRepository.persist(coordenador);
        return coordenador;
    }

    @Transactional
    public Coordenador atualizarCoordenador(Long id, Coordenador coordenadorAtualizado) {
        Coordenador coordenadorExistente = coordenadorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Coordenador com ID " + id + " não encontrado."));

        // Atualiza os campos
        coordenadorExistente.setNome(coordenadorAtualizado.getNome());
        coordenadorExistente.setRegistro(coordenadorAtualizado.getRegistro());

        // O Panache detecta as alterações e as persiste na transação
        return coordenadorExistente;
    }

    @Transactional
    public boolean deletarCoordenador(Long id) {
        return coordenadorRepository.deleteById(id);
    }
}
