package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.Aluno;
import com.glaydson.controleacademico.domain.model.Curso; // Importar Curso
import com.glaydson.controleacademico.domain.repository.AlunoRepository;
import com.glaydson.controleacademico.domain.repository.CursoRepository; // Importar CursoRepository
import com.glaydson.controleacademico.rest.dto.AlunoRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
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
     * @param alunoDto O objeto Aluno a ser criado. O ID deve ser nulo.
     * @return O Aluno persistido com o ID gerado.
     * @throws BadRequestException Se o curso associado não for encontrado.
     */
    @Transactional
    public Aluno criarAluno(AlunoRequestDTO alunoDto) { // <-- Recebe o DTO
        if (alunoRepository.find("matricula", alunoDto.matricula).count() > 0) {
            throw new BadRequestException("Já existe um aluno com a matrícula " + alunoDto.matricula);
        }

        Curso cursoExistente = cursoRepository.findByIdOptional(alunoDto.cursoId) // <-- Usa cursoId do DTO
                .orElseThrow(() -> new NotFoundException("Curso com ID " + alunoDto.cursoId + " não encontrado."));

        Aluno aluno = new Aluno(); // Cria uma nova instância da entidade
        aluno.nome = alunoDto.nome; // Atribui os campos do DTO à entidade
        aluno.matricula = alunoDto.matricula;
        aluno.curso = cursoExistente; // Associa a instância gerenciada do Curso

        alunoRepository.persist(aluno);
        return aluno;
    }

    /**
     * Atualiza um aluno existente.
     * @param id O ID do aluno a ser atualizado.
     * @param alunoDto O objeto Aluno com os dados atualizados.
     * @return O Aluno atualizado.
     * @throws NotFoundException Se o aluno com o ID especificado não for encontrado.
     * @throws BadRequestException Se o curso associado não for encontrado.
     */
    @Transactional
    public Aluno atualizarAluno(Long id, AlunoRequestDTO alunoDto) { // Assumindo AlunoUpdateDTO
        Aluno alunoExistente = alunoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Aluno com ID " + id + " não encontrado."));

        // Validação de unicidade da matrícula (se for alterada)
        if (!alunoExistente.matricula.equals(alunoDto.matricula) &&
                alunoRepository.find("matricula", alunoDto.matricula).count() > 0) {
            throw new BadRequestException("Já existe outro aluno com a matrícula " + alunoDto.matricula);
        }

        alunoExistente.nome = alunoDto.nome;
        alunoExistente.matricula = alunoDto.matricula;

        if (alunoDto.cursoId != null) { // Permite atualizar o curso
            Curso novoCurso = cursoRepository.findByIdOptional(alunoDto.cursoId)
                    .orElseThrow(() -> new NotFoundException("Curso com ID " + alunoDto.cursoId + " não encontrado."));
            alunoExistente.curso = novoCurso;
        } else {
            // Se o cursoId for nulo no DTO, o que fazer? Lançar erro? Deixar como está?
            // Como regra, mantém-se o curso como obrigatório.
            throw new BadRequestException("O ID do curso é obrigatório para atualização.");
        }
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