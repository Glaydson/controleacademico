package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Aluno;
import com.glaydson.controleacademico.domain.model.Curso; // Importar Curso
import com.glaydson.controleacademico.domain.repository.AlunoRepository;
import com.glaydson.controleacademico.domain.repository.CursoRepository; // Importar CursoRepository
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional; // Importante para operações de escrita
import jakarta.ws.rs.NotFoundException; // Para lançar exceções quando um recurso não é encontrado
import jakarta.ws.rs.BadRequestException; // Para erros de requisição inválida

import java.util.List;
import java.util.Optional;

@ApplicationScoped // Marca a classe como um bean gerenciado pelo CDI (Contexts and Dependency Injection)
public class AlunoService {

    AlunoRepository alunoRepository;

    CursoRepository cursoRepository;

    public AlunoService(AlunoRepository alunoRepository, CursoRepository cursoRepository) {
        this.alunoRepository = alunoRepository; // Injeção de dependência do repositório de Alunos
        this.cursoRepository = cursoRepository; // Injeção de dependência do repositório de Cursos
    }

    /**
     * Lista todos os alunos.
     * @return Uma lista de Alunos.
     */
    public List<Aluno> listarTodosAlunos() {
        return alunoRepository.listAll(); // Método Panache para listar tudo
    }

    /**
     * Busca um aluno pelo ID.
     * @param id O ID do aluno.
     * @return O Aluno encontrado, ou Optional.empty() se não existir.
     */
    public Optional<Aluno> buscarAlunoPorId(Long id) {
        return alunoRepository.findByIdOptional(id); // Método Panache para buscar por ID com Optional
    }

    /**
     * Cria um novo aluno.
     * @param aluno O objeto Aluno a ser criado. O ID deve ser nulo.
     * @return O Aluno persistido com o ID gerado.
     * @throws BadRequestException Se o curso associado não for encontrado.
     */
    @Transactional // Marca o método para ser executado dentro de uma transação de banco de dados
    public Aluno criarAluno(Aluno aluno) {
        if (aluno.getId() != null) {
            throw new BadRequestException("ID deve ser nulo para criar um novo aluno.");
        }
        // Validação adicional: Garantir que o curso existe
        if (aluno.getCurso() == null || aluno.getCurso().id == null) {
            throw new BadRequestException("Um aluno deve estar associado a um curso válido.");
        }

        Curso cursoExistente = cursoRepository.findByIdOptional(aluno.getCurso().id)
                .orElseThrow(() -> new NotFoundException("Curso com ID " + aluno.getCurso().id + " não encontrado."));
        aluno.setCurso(cursoExistente); // Associa a instância gerenciada do Curso

        alunoRepository.persist(aluno); // Persiste o aluno no banco de dados
        return aluno;
    }

    /**
     * Atualiza um aluno existente.
     * @param id O ID do aluno a ser atualizado.
     * @param alunoAtualizado O objeto Aluno com os dados atualizados.
     * @return O Aluno atualizado.
     * @throws NotFoundException Se o aluno com o ID especificado não for encontrado.
     * @throws BadRequestException Se o curso associado não for encontrado.
     */
    @Transactional
    public Aluno atualizarAluno(Long id, Aluno alunoAtualizado) {
        Aluno alunoExistente = alunoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Aluno com ID " + id + " não encontrado."));

        // Atualiza os campos do aluno existente com os dados do alunoAtualizado
        alunoExistente.setNome(alunoAtualizado.getNome());
        alunoExistente.setMatricula(alunoAtualizado.getMatricula());

        // Atualiza o curso se for fornecido um novo curso ID
        if (alunoAtualizado.getCurso() != null && alunoAtualizado.getCurso().id != null) {
            Curso novoCurso = cursoRepository.findByIdOptional(alunoAtualizado.getCurso().id)
                    .orElseThrow(() -> new NotFoundException("Curso com ID " + alunoAtualizado.getCurso().id + " não encontrado."));
            alunoExistente.setCurso(novoCurso);
        } else if (alunoAtualizado.getCurso() == null) {
            // Lógica para lidar com a remoção de um curso, se permitido, ou erro se for obrigatório
            throw new BadRequestException("Um aluno deve estar associado a um curso válido.");
        }

        // Panache automaticamente persiste as alterações em entidades gerenciadas dentro de uma transação.
        // alunoRepository.persist(alunoExistente); // Esta linha é opcional, pois as alterações já são rastreadas
        return alunoExistente;
    }

    /**
     * Deleta um aluno pelo ID.
     * @param id O ID do aluno a ser deletado.
     * @return true se o aluno foi deletado, false caso contrário.
     */
    @Transactional
    public boolean deletarAluno(Long id) {
        return alunoRepository.deleteById(id); // Método Panache para deletar por ID
    }

    /**
     * Busca um aluno pela matrícula.
     * @param matricula A matrícula do aluno.
     * @return O Aluno encontrado, ou Optional.empty() se não existir.
     */
    public Optional<Aluno> buscarAlunoPorMatricula(String matricula) {
        return alunoRepository.find("matricula", matricula).firstResultOptional();
    }
}