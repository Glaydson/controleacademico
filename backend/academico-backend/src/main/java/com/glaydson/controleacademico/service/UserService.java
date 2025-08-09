package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.*;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.rest.dto.UserCreateRequestDTO;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class UserService {

    @ConfigProperty(name = "quarkus.keycloak.admin-client.server-url")
    String serverUrl;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-secret")
    String clientSecret;

    @Inject
    CursoRepository cursoRepository;

    @Inject
    DisciplinaRepository disciplinaRepository;

    private Keycloak getKeycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    @Transactional
    public void createUser(UserCreateRequestDTO requestDTO) {
        // Validate business logic FIRST, before initializing Keycloak
        validateUserRequest(requestDTO);

        Keycloak keycloak = null;

        try {
            keycloak = getKeycloakAdminClient();
        } catch (ExceptionInInitializerError e) {
            throw new RuntimeException("Keycloak admin client initialization failed - class initialization error: " + e.getMessage());
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("Keycloak admin client initialization failed - missing dependencies: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Keycloak admin client: " + e.getMessage());
        }

        try {
            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(requestDTO.getEmail());
            user.setEmail(requestDTO.getEmail());
            user.setFirstName(requestDTO.getNome());
            user.setEnabled(true);

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(requestDTO.getPassword());
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Create user in Keycloak
            Response response = keycloak.realm(realm).users().create(user);
            if (response.getStatus() != 201) {
                throw new WebApplicationException("Error creating user in Keycloak", response);
            }

            // Extract user ID from response location
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            response.close();

            // Assign role to user
            RoleRepresentation roleToAdd = keycloak.realm(realm)
                    .roles().get(requestDTO.getRole()).toRepresentation();

            if (roleToAdd == null) {
                throw new WebApplicationException("Role not found in Keycloak", Response.Status.BAD_REQUEST);
            }

            keycloak.realm(realm).users().get(userId)
                    .roles().realmLevel().add(Collections.singletonList(roleToAdd));

            // Save user locally based on role
            saveLocalUser(requestDTO, userId);

        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }

    private void validateUserRequest(UserCreateRequestDTO requestDTO) {
        // First, validate that the request DTO is not null
        if (requestDTO == null) {
            throw new WebApplicationException("Request data is required", Response.Status.BAD_REQUEST);
        }

        // Validate role
        if (requestDTO.getRole() == null || requestDTO.getRole().trim().isEmpty()) {
            throw new WebApplicationException("Role is required", Response.Status.BAD_REQUEST);
        }

        String role = requestDTO.getRole().toUpperCase();
        if (!role.equals("ALUNO") && !role.equals("PROFESSOR") && !role.equals("COORDENADOR")) {
            throw new WebApplicationException("Invalid role specified", Response.Status.BAD_REQUEST);
        }

        // Validate required fields based on role
        switch (role) {
            case "ALUNO":
            case "COORDENADOR":
                if (requestDTO.getCursoId() == null) {
                    throw new WebApplicationException("Curso ID is required for " + role, Response.Status.BAD_REQUEST);
                }
                // Validate that the course exists
                if (!cursoRepository.findByIdOptional(requestDTO.getCursoId()).isPresent()) {
                    throw new WebApplicationException("Curso not found", Response.Status.BAD_REQUEST);
                }
                break;
            case "PROFESSOR":
                if (requestDTO.getDisciplinaIds() == null || requestDTO.getDisciplinaIds().isEmpty()) {
                    throw new WebApplicationException("Disciplina IDs are required for PROFESSOR", Response.Status.BAD_REQUEST);
                }
                // Validate that all disciplines exist
                for (Long disciplinaId : requestDTO.getDisciplinaIds()) {
                    if (!disciplinaRepository.findByIdOptional(disciplinaId).isPresent()) {
                        throw new WebApplicationException("Disciplina not found: " + disciplinaId, Response.Status.BAD_REQUEST);
                    }
                }
                break;
        }

        // Validate other required fields
        if (requestDTO.getNome() == null || requestDTO.getNome().trim().isEmpty()) {
            throw new WebApplicationException("Nome is required", Response.Status.BAD_REQUEST);
        }
        if (requestDTO.getEmail() == null || requestDTO.getEmail().trim().isEmpty()) {
            throw new WebApplicationException("Email is required", Response.Status.BAD_REQUEST);
        }
        if (requestDTO.getPassword() == null || requestDTO.getPassword().trim().isEmpty()) {
            throw new WebApplicationException("Password is required", Response.Status.BAD_REQUEST);
        }
        if (requestDTO.getMatricula() == null || requestDTO.getMatricula().trim().isEmpty()) {
            throw new WebApplicationException("Matricula is required", Response.Status.BAD_REQUEST);
        }
    }

    private void saveLocalUser(UserCreateRequestDTO requestDTO, String userId) {
        switch (requestDTO.getRole().toUpperCase()) {
            case "ALUNO":
                createAluno(requestDTO, userId);
                break;
            case "PROFESSOR":
                createProfessor(requestDTO, userId);
                break;
            case "COORDENADOR":
                createCoordenador(requestDTO, userId);
                break;
            default:
                throw new WebApplicationException("Invalid role specified", Response.Status.BAD_REQUEST);
        }
    }

    private void createAluno(UserCreateRequestDTO dto, String keycloakId) {
        Curso curso = cursoRepository.findByIdOptional(dto.getCursoId())
                .orElseThrow(() -> new WebApplicationException("Curso not found", Response.Status.BAD_REQUEST));
        Aluno aluno = new Aluno(dto.getNome(), dto.getMatricula(), curso);
        aluno.keycloakId = keycloakId;
        aluno.persist();
    }

    private void createProfessor(UserCreateRequestDTO dto, String keycloakId) {
        Set<Disciplina> disciplinas = new HashSet<>(disciplinaRepository.list("id in ?1", dto.getDisciplinaIds()));
        Professor professor = new Professor(dto.getNome(), dto.getMatricula(), disciplinas);
        professor.keycloakId = keycloakId;
        professor.persist();
    }

    private void createCoordenador(UserCreateRequestDTO dto, String keycloakId) {
        if (dto.getCursoId() == null) {
            throw new WebApplicationException("Curso ID is required for Coordenador", Response.Status.BAD_REQUEST);
        }
        Curso curso = cursoRepository.findByIdOptional(dto.getCursoId())
                .orElseThrow(() -> new WebApplicationException("Curso not found", Response.Status.BAD_REQUEST));
        Coordenador coordenador = new Coordenador(dto.getNome(), dto.getMatricula(), curso);
        coordenador.keycloakId = keycloakId;
        coordenador.persist();
    }
}
