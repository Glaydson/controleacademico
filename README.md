# Aplicação Controle Acadêmico - V1 (em construção)

Este projeto consiste em uma aplicação de Controle Acadêmico, que permite a gestão de cursos. Possui como usuários básicos 
Coordenadores, Professores e Alunos. 

A aplicação tem como caraterísticas principais:
Frontend desenvolvido em Angular 20, com autenticação usando keycloak;
Backend desenvolvido em Java 21, utilizando Quarkus como mecanismo para acesso aos endpoints REST, e banco de dados PostgreSQL;
A aplicação é containerizada usando Docker e Docker Compose.

## Pré-requisitos

- Java 21
- Quarkus
- Maven
- PostgreSQL
- keycloak
- Docker
- Docker Compose

## Iniciando o Projeto

### Clonar o Repositório 

```sh
git clone https://github.com/Glaydson/controleacademico
cd controleacademico
```

## Rodando em um Docker Container

Para executar a aplicação usando Docker Compose, execute o comando na pasta raiz controleacademico:

```sh
docker-compose up --build
```
Isto iniciará a aplicação frontend, a aplicação backend, o servidor keycloak, o banco de dados keycloak e o banco de dados PostgreSQL

### Accessar a API backend

A API estará disponível em `http://localhost:8081/`.

## API Endpoints

- `POST /alunos`: Cria um novo aluno.
- `GET /alunos/{id}`: Obtém um aluno pelo ID.
- `GET /alunos`: Obtém todos os alunos.
- `PUT /alunos/{id}`: Atualiza um aluno pelo ID.
- `DELETE /alunos/{id}`: Deleta um aluno pelo ID.
- `POST /professores`: Cria um novo professor.
- `GET /professores/{id}`: Obtém um professor pelo ID.
- `GET /professores`: Obtém todos os professores.
- `PUT /professores/{id}`: Atualiza um professor pelo ID.
- `DELETE /professores/{id}`: Deleta um professor pelo ID.
- `POST /coordenadores`: Cria um novo coordenador.
- `GET /coordenadores/{id}`: Obtém um coordenador pelo ID.
- `GET /coordenadores`: Obtém todos os coordenadores.
- `PUT /coordenadores/{id}`: Atualiza um coordenador pelo ID.
- `DELETE /coordenadores/{id}`: Deleta um coordenador pelo ID.
- `POST /cursos`: Cria um novo curso.
- `GET /cursos/{id}`: Obtém um curso pelo ID.
- `GET /cursos`: Obtém todos os cursos.
- `PUT /cursos/{id}`: Atualiza um curso pelo ID.
- `DELETE /cursos/{id}`: Deleta um curso pelo ID.
- `POST /disciplinas`: Cria uma nova disciplina.
- `GET /disciplinas/{id}`: Obtém uma disciplina pelo ID.
- `GET /disciplinas`: Obtém todas as disciplinas.
- `PUT /disciplinas/{id}`: Atualiza uma disciplina pelo ID.
- `DELETE /disciplinas/{id}`: Deleta uma disciplina pelo ID.
- `POST /semestres`: Cria um novo semestre.
- `GET /semestres/{id}`: Obtém um semestre pelo ID.
- `GET /semestres`: Obtém todos os semestres.
- `PUT /semestres/{id}`: Atualiza um semestre pelo ID.
- `DELETE /semestres/{id}`: Deleta um semestre pelo ID.
- `POST /matrizcurricular`: Cria uma nova matríz curricular.
- `GET /matrizcurricular/{id}`: Obtém uma matríz curricular pelo ID.
- `PUT /matrizcurricular/{id}`: Atualiza uma matríz curricular pelo ID.
- `DELETE /matrizcurricular/{id}`: Deleta uma matríz curricular pelo ID.

## Configuração

### Database 

