package com.glaydson.controleacademico.service;

import com.glaydson.controleacademico.domain.model.*;
import com.glaydson.controleacademico.domain.repository.CursoRepository;
import com.glaydson.controleacademico.domain.repository.DisciplinaRepository;
import com.glaydson.controleacademico.rest.dto.UserCreateRequestDTO;
import com.glaydson.controleacademico.rest.dto.UserResponseDTO;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class UserService {

    @ConfigProperty(name = "keycloak.admin.server-url")
    String serverUrl;

    @ConfigProperty(name = "keycloak.admin.realm")
    String realm;

    @ConfigProperty(name = "keycloak.admin.client-id")
    String clientId;

    @ConfigProperty(name = "keycloak.admin.client-secret")
    String clientSecret;

    CursoRepository cursoRepository;
    DisciplinaRepository disciplinaRepository;

    public UserService(CursoRepository cursoRepository, DisciplinaRepository disciplinaRepository) {
        this.cursoRepository = cursoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    private Keycloak getKeycloakAdminClient() {
        // Detect if running in Docker container and adjust server URL accordingly
        String effectiveServerUrl = getEffectiveKeycloakUrl();

        return KeycloakBuilder.builder()
                .serverUrl(effectiveServerUrl)
                .realm(realm)
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    private String getEffectiveKeycloakUrl() {
        // Check if we're running in a Docker container
        if (isRunningInDocker()) {
            // Use container hostname for internal communication
            String dockerUrl = serverUrl.replace("localhost", "keycloak");
            System.out.println("=== Detected Docker environment, using URL: " + dockerUrl + " ===");
            return dockerUrl;
        } else {
            System.out.println("=== Using configured server URL: " + serverUrl + " ===");
            return serverUrl;
        }
    }

    private boolean isRunningInDocker() {
        try {
            // Check for Docker-specific environment indicators
            String hostname = System.getenv("HOSTNAME");
            String dockerEnv = System.getenv("DOCKER_CONTAINER");
            String containerName = System.getenv("COMPOSE_SERVICE");

            System.out.println("=== Docker Detection Debug ===");
            System.out.println("HOSTNAME: " + hostname);
            System.out.println("DOCKER_CONTAINER: " + dockerEnv);
            System.out.println("COMPOSE_SERVICE: " + containerName);

            // Check if hostname looks like a Docker container ID or contains common Docker patterns
            boolean dockerHostname = hostname != null && (
                (hostname.length() == 12 && hostname.matches("^[a-f0-9]+$")) ||
                hostname.contains("controle_academico") ||
                hostname.contains("academico")
            );

            // Check for .dockerenv file (standard Docker indicator)
            boolean dockerEnvFile = new java.io.File("/.dockerenv").exists();

            // Check for cgroup Docker indicators
            boolean dockerCgroup = false;
            try {
                String cgroup = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("/proc/1/cgroup")));
                dockerCgroup = cgroup.contains("docker") || cgroup.contains("containerd") || cgroup.contains("kubepods");
            } catch (Exception e) {
                // Ignore - not running on Linux or can't read cgroup
                System.out.println("Could not read cgroup info: " + e.getMessage());
            }

            // Additional check: if we can't reach localhost:8080 but serverUrl contains localhost, assume Docker
            boolean networkIndicatesDocker = false;
            if (serverUrl.contains("localhost")) {
                try {
                    java.net.Socket socket = new java.net.Socket();
                    socket.connect(new java.net.InetSocketAddress("localhost", 8080), 2000);
                    socket.close();
                    // If we can connect to localhost:8080, we're probably not in Docker
                    networkIndicatesDocker = false;
                } catch (Exception e) {
                    // If we can't connect to localhost:8080, we might be in Docker
                    networkIndicatesDocker = true;
                    System.out.println("Cannot connect to localhost:8080, assuming Docker environment");
                }
            }

            boolean isDocker = dockerHostname || dockerEnvFile || dockerCgroup || "true".equals(dockerEnv) || networkIndicatesDocker;

            System.out.println("=== Docker detection results ===");
            System.out.println("dockerHostname: " + dockerHostname);
            System.out.println("dockerEnvFile: " + dockerEnvFile);
            System.out.println("dockerCgroup: " + dockerCgroup);
            System.out.println("networkIndicatesDocker: " + networkIndicatesDocker);
            System.out.println("Final result: " + isDocker);
            System.out.println("=== End Docker Detection ===");

            return isDocker;
        } catch (Exception e) {
            System.err.println("=== Error detecting Docker environment: " + e.getMessage() + " ===");
            // If we can't detect properly and can't reach localhost, assume Docker
            return serverUrl.contains("localhost");
        }
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO requestDTO) {
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

            // Create and return UserResponseDTO with the created user data
            UserResponseDTO userResponse = new UserResponseDTO();
            userResponse.setId(userId);
            userResponse.setNome(requestDTO.getNome());
            userResponse.setEmail(requestDTO.getEmail());
            userResponse.setMatricula(requestDTO.getMatricula());
            userResponse.setRole(requestDTO.getRole());
            userResponse.setEnabled(true);

            // Set role-specific data
            switch (requestDTO.getRole().toUpperCase()) {
                case "ALUNO":
                case "COORDENADOR":
                    if (requestDTO.getCursoId() != null) {
                        userResponse.setCursoId(requestDTO.getCursoId());
                        var curso = cursoRepository.findByIdOptional(requestDTO.getCursoId());
                        if (curso.isPresent()) {
                            userResponse.setCursoNome(((com.glaydson.controleacademico.domain.model.Curso) curso.get()).nome);
                        }
                    }
                    break;
                case "PROFESSOR":
                    if (requestDTO.getDisciplinaIds() != null && !requestDTO.getDisciplinaIds().isEmpty()) {
                        userResponse.setDisciplinaIds(requestDTO.getDisciplinaIds());
                        var disciplinas = disciplinaRepository.list("id in ?1", requestDTO.getDisciplinaIds());
                        if (!disciplinas.isEmpty()) {
                            userResponse.setDisciplinaNomes(disciplinas.stream()
                                .map(d -> ((com.glaydson.controleacademico.domain.model.Disciplina) d).nome)
                                .collect(java.util.stream.Collectors.toSet()));
                        }
                    }
                    break;
            }

            return userResponse;

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
                // Disciplinas are now optional for Professor creation
                // If disciplinaIds are provided, validate that all disciplines exist
                if (requestDTO.getDisciplinaIds() != null && !requestDTO.getDisciplinaIds().isEmpty()) {
                    for (Long disciplinaId : requestDTO.getDisciplinaIds()) {
                        if (!disciplinaRepository.findByIdOptional(disciplinaId).isPresent()) {
                            throw new WebApplicationException("Disciplina not found: " + disciplinaId, Response.Status.BAD_REQUEST);
                        }
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
        Aluno aluno = new Aluno(dto.getNome(), dto.getMatricula(), curso, keycloakId);
        aluno.persist();
    }

    private void createProfessor(UserCreateRequestDTO dto, String keycloakId) {
        // Handle optional disciplinas - create empty set if none provided
        Set<Disciplina> disciplinas = new HashSet<>();
        if (dto.getDisciplinaIds() != null && !dto.getDisciplinaIds().isEmpty()) {
            disciplinas = new HashSet<>(disciplinaRepository.list("id in ?1", dto.getDisciplinaIds()));
        }
        Professor professor = new Professor(dto.getNome(), dto.getMatricula(), disciplinas, keycloakId);
        professor.persist();
    }

    private void createCoordenador(UserCreateRequestDTO dto, String keycloakId) {
        if (dto.getCursoId() == null) {
            throw new WebApplicationException("Curso ID is required for Coordenador", Response.Status.BAD_REQUEST);
        }
        Curso curso = cursoRepository.findByIdOptional(dto.getCursoId())
                .orElseThrow(() -> new WebApplicationException("Curso not found", Response.Status.BAD_REQUEST));
        Coordenador coordenador = new Coordenador(dto.getNome(), dto.getMatricula(), curso, keycloakId);
        coordenador.persist();
    }

    // New methods for user management

    public List<UserResponseDTO> getAllUsers() {
        System.out.println("=== getAllUsers() called ===");
        System.out.println("Server URL: " + serverUrl);
        System.out.println("Realm: " + realm);
        System.out.println("Client ID: " + clientId);
        System.out.println("Client Secret: " + (clientSecret != null ? "***" + clientSecret.substring(clientSecret.length()-4) : "null"));

        // First, test basic connectivity to Keycloak
        if (!isKeycloakReachable()) {
            System.err.println("=== Keycloak server is not reachable at " + serverUrl + " ===");
            throw new RuntimeException("Keycloak server is not reachable at " + serverUrl + ". Please ensure Keycloak is running and accessible.");
        }

        Keycloak keycloak = null;
        try {
            System.out.println("=== Attempting to create Keycloak admin client ===");
            keycloak = getKeycloakAdminClient();
            System.out.println("=== Keycloak admin client created successfully ===");

            System.out.println("=== Attempting to list users from Keycloak ===");
            var allUsers = keycloak.realm(realm).users().list();
            System.out.println("=== Retrieved " + allUsers.size() + " total users from Keycloak ===");

            java.util.List<UserResponseDTO> result = new java.util.ArrayList<>();
            java.util.Set<String> relevantRoles = java.util.Set.of("COORDENADOR", "ALUNO", "PROFESSOR", "ADMIN");
            int filteredCount = 0;

            for (org.keycloak.representations.idm.UserRepresentation user : allUsers) {
                System.out.println("Processing user: " + user.getUsername());

                // Get user roles to check if user has relevant application roles
                var realmRoles = keycloak.realm(realm).users().get(user.getId()).roles().realmLevel().listEffective();
                String userRole = null;
                boolean hasRelevantRole = false;

                if (!realmRoles.isEmpty()) {
                    // Find first relevant application role (prioritize app roles over default roles)
                    for (var role : realmRoles) {
                        System.out.println("Checking Role: " + role.getName());
                        String roleName = role.getName();
                        if (relevantRoles.contains(roleName)) {
                            System.out.println("Found Relevant Role: " + roleName);
                            userRole = roleName;
                            hasRelevantRole = true;
                            break; // Stop at first relevant role found
                        }
                    }
                }

                // Only include users with relevant application roles
                if (hasRelevantRole) {
                    System.out.println("Found Relevant Role: " + userRole);
                    var userDto = new UserResponseDTO();
                    userDto.setId(user.getId());
                    userDto.setNome(user.getFirstName());
                    userDto.setEmail(user.getEmail());
                    userDto.setEnabled(user.isEnabled());
                    userDto.setRole(userRole);

                    // Get matricula from local database based on role
                    populateLocalUserData(userDto);

                    result.add(userDto);
                    filteredCount++;
                    System.out.println("Added user " + user.getUsername() + " with role: " + userRole);
                } else {
                    System.out.println("Skipped user " + user.getUsername() + " - no relevant application role");
                }
            }
            
            System.out.println("=== Filtered " + filteredCount + " relevant users from " + allUsers.size() + " total users ===");
            System.out.println("=== Returning " + result.size() + " users ===");
            return result;
        } catch (Exception e) {
            System.err.println("=== ERROR in getAllUsers(): " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();

            // Provide more specific error messages
            if (e.getMessage().contains("Connection refused")) {
                throw new RuntimeException("Cannot connect to Keycloak server. Please ensure Keycloak is running and accessible.", e);
            } else if (e.getMessage().contains("Unauthorized") || e.getMessage().contains("401")) {
                throw new RuntimeException("Authentication failed with Keycloak. Please check client credentials configuration.", e);
            } else if (e.getMessage().contains("Forbidden") || e.getMessage().contains("403")) {
                throw new RuntimeException("Access denied to Keycloak Admin API. The client 'academico-backend' needs to be configured with admin permissions in Keycloak. " +
                    "Please ensure the client has 'Service Account Enabled' and appropriate admin roles assigned.", e);
            } else if (e.getMessage().contains("realm") || e.getMessage().contains("404")) {
                throw new RuntimeException("Keycloak realm '" + realm + "' not found. Please check realm configuration.", e);
            } else {
                throw new RuntimeException("Failed to retrieve users from Keycloak: " + e.getMessage(), e);
            }
        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }

    private boolean isKeycloakReachable() {
        // Use the same URL detection logic as the admin client
        String effectiveUrl = getEffectiveKeycloakUrl();

        try {
            java.net.URL url = new java.net.URL(effectiveUrl + "/realms/" + realm);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            System.out.println("=== Keycloak reachability check using " + effectiveUrl + ": HTTP " + responseCode + " ===");
            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("=== Keycloak reachability check failed using " + effectiveUrl + ": " + e.getMessage() + " ===");
            return false;
        }
    }

    public UserResponseDTO getUserById(String id) {
        Keycloak keycloak = null;
        try {
            keycloak = getKeycloakAdminClient();
            
            var user = keycloak.realm(realm).users().get(id).toRepresentation();

            // Get user roles and prioritize relevant application roles
            var realmRoles = keycloak.realm(realm).users().get(user.getId()).roles().realmLevel().listEffective();
            java.util.Set<String> relevantRoles = java.util.Set.of("COORDENADOR", "ALUNO", "PROFESSOR", "ADMIN");
            String userRole = null;
            boolean hasRelevantRole = false;

            if (!realmRoles.isEmpty()) {
                // Find first relevant application role (prioritize app roles over default roles)
                for (var role : realmRoles) {
                    String roleName = role.getName();
                    if (relevantRoles.contains(roleName)) {
                        userRole = roleName;
                        hasRelevantRole = true;
                        break; // Stop at first relevant role found
                    }
                }

                // If no relevant role found, log all roles for debugging
                if (!hasRelevantRole) {
                    System.out.println("User " + user.getUsername() + " has no relevant application roles. Available roles:");
                    for (var role : realmRoles) {
                        System.out.println("  - " + role.getName());
                    }
                }
            }
            
            // Only return user if they have a relevant application role
            if (!hasRelevantRole) {
                throw new WebApplicationException("User does not have a relevant application role", Response.Status.NOT_FOUND);
            }

            var userDto = new UserResponseDTO();
            userDto.setId(user.getId());
            userDto.setNome(user.getFirstName());
            userDto.setEmail(user.getEmail());
            userDto.setEnabled(user.isEnabled());
            userDto.setRole(userRole);

            populateLocalUserData(userDto);
            
            return userDto;
        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }

    @Transactional
    public void updateUser(String id, UserCreateRequestDTO requestDTO) {
        Keycloak keycloak = null;
        try {
            keycloak = getKeycloakAdminClient();
            
            // Update user in Keycloak
            var user = keycloak.realm(realm).users().get(id).toRepresentation();
            user.setFirstName(requestDTO.getNome());
            user.setEmail(requestDTO.getEmail());
            user.setUsername(requestDTO.getEmail());
            
            keycloak.realm(realm).users().get(id).update(user);
            
            // Update password if provided
            if (requestDTO.getPassword() != null && !requestDTO.getPassword().trim().isEmpty()) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(requestDTO.getPassword());
                credential.setTemporary(false);
                keycloak.realm(realm).users().get(id).resetPassword(credential);
            }
            
            // Update roles if needed
            updateUserRoles(keycloak, id, requestDTO.getRole());
            
            // Update local data
            updateLocalUser(id, requestDTO);
            
        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }

    @Transactional
    public void deleteUser(String id) {
        Keycloak keycloak = null;
        try {
            keycloak = getKeycloakAdminClient();
            
            // Delete from local database first
            deleteLocalUser(id);
            
            // Delete from Keycloak
            keycloak.realm(realm).users().get(id).remove();
            
        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }

    private void populateLocalUserData(UserResponseDTO userDto) {
        String role = userDto.getRole();
        String keycloakId = userDto.getId();
        
        if (role != null) {
            switch (role.toUpperCase()) {
                case "ALUNO":
                    var aluno = Aluno.find("keycloakId", keycloakId).firstResult();
                    if (aluno != null) {
                        userDto.setMatricula(((Aluno) aluno).matricula);
                        if (((Aluno) aluno).curso != null) {
                            userDto.setCursoId(((Aluno) aluno).curso.id);
                            userDto.setCursoNome(((Aluno) aluno).curso.nome);
                        }
                    }
                    break;
                case "PROFESSOR":
                    var professor = Professor.find("keycloakId", keycloakId).firstResult();
                    if (professor != null) {
                        userDto.setMatricula(((Professor) professor).getRegistro());
                        if (((Professor) professor).disciplinas != null) {
                            userDto.setDisciplinaIds(((Professor) professor).disciplinas.stream()
                                .map(d -> d.id).collect(java.util.stream.Collectors.toSet()));
                            userDto.setDisciplinaNomes(((Professor) professor).disciplinas.stream()
                                .map(d -> d.nome).collect(java.util.stream.Collectors.toSet()));
                        }
                    }
                    break;
                case "COORDENADOR":
                    var coordenador = Coordenador.find("keycloakId", keycloakId).firstResult();
                    if (coordenador != null) {
                        userDto.setMatricula(((Coordenador) coordenador).getRegistro());
                        if (((Coordenador) coordenador).curso != null) {
                            userDto.setCursoId(((Coordenador) coordenador).curso.id);
                            userDto.setCursoNome(((Coordenador) coordenador).curso.nome);
                        }
                    }
                    break;
            }
        }
    }

    private void updateUserRoles(Keycloak keycloak, String userId, String newRole) {
        // Remove existing roles
        var currentRoles = keycloak.realm(realm).users().get(userId).roles().realmLevel().listEffective();
        java.util.List<RoleRepresentation> rolesToRemove = new java.util.ArrayList<>();
        
        for (var role : currentRoles) {
            if (role.getName().equals("ALUNO") || role.getName().equals("PROFESSOR") || role.getName().equals("COORDENADOR")) {
                rolesToRemove.add(role);
            }
        }
        
        if (!rolesToRemove.isEmpty()) {
            keycloak.realm(realm).users().get(userId).roles().realmLevel().remove(rolesToRemove);
        }
        
        // Add new role
        RoleRepresentation newRoleRep = keycloak.realm(realm).roles().get(newRole).toRepresentation();
        if (newRoleRep != null) {
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Collections.singletonList(newRoleRep));
        }
    }

    private void updateLocalUser(String keycloakId, UserCreateRequestDTO requestDTO) {
        // Remove from old role tables
        deleteLocalUser(keycloakId);
        
        // Add to new role table
        saveLocalUser(requestDTO, keycloakId);
    }

    private void deleteLocalUser(String keycloakId) {
        // Delete from all role tables
        Aluno.delete("keycloakId", keycloakId);
        Professor.delete("keycloakId", keycloakId);
        Coordenador.delete("keycloakId", keycloakId);
    }

    public java.util.List<String> getAvailableAdminRoles() {
        System.out.println("=== getAvailableAdminRoles() called ===");

        Keycloak keycloak = null;
        try {
            keycloak = getKeycloakAdminClient();

            // Try to get realm-management client roles
            try {
                var realmManagementClient = keycloak.realm(realm).clients().findByClientId("realm-management");
                if (!realmManagementClient.isEmpty()) {
                    var clientId = realmManagementClient.get(0).getId();
                    var roles = keycloak.realm(realm).clients().get(clientId).roles().list();

                    System.out.println("=== Available realm-management roles: ===");
                    java.util.List<String> roleNames = new java.util.ArrayList<>();
                    for (var role : roles) {
                        System.out.println("Role: " + role.getName() + " - " + role.getDescription());
                        roleNames.add(role.getName() + " (" + role.getDescription() + ")");
                    }
                    return roleNames;
                }
            } catch (Exception e) {
                System.err.println("Could not get realm-management roles: " + e.getMessage());
            }

            // Fallback: try to get realm roles
            try {
                var realmRoles = keycloak.realm(realm).roles().list();
                System.out.println("=== Available realm roles: ===");
                java.util.List<String> roleNames = new java.util.ArrayList<>();
                for (var role : realmRoles) {
                    if (role.getName().contains("admin") || role.getName().contains("user") || role.getName().contains("manage")) {
                        System.out.println("Role: " + role.getName() + " - " + role.getDescription());
                        roleNames.add("REALM:" + role.getName() + " (" + role.getDescription() + ")");
                    }
                }
                return roleNames;
            } catch (Exception e) {
                System.err.println("Could not get realm roles: " + e.getMessage());
            }

            return java.util.Arrays.asList("Unable to retrieve roles - check permissions");

        } catch (Exception e) {
            System.err.println("=== ERROR in getAvailableAdminRoles(): " + e.getMessage());
            return java.util.Arrays.asList("Error: " + e.getMessage());
        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }

    @Transactional
    public void syncKeycloakUsersToBackend() {
        System.out.println("=== Starting Keycloak to Backend user sync ===");

        Keycloak keycloak = null;
        try {
            keycloak = getKeycloakAdminClient();
            var allUsers = keycloak.realm(realm).users().list();
            java.util.Set<String> relevantRoles = java.util.Set.of("COORDENADOR", "ALUNO", "PROFESSOR", "ADMIN");

            int syncedCount = 0;
            int skippedCount = 0;

            for (org.keycloak.representations.idm.UserRepresentation user : allUsers) {
                try {
                    // Get user roles
                    var realmRoles = keycloak.realm(realm).users().get(user.getId()).roles().realmLevel().listEffective();
                    String userRole = null;

                    for (var role : realmRoles) {
                        if (relevantRoles.contains(role.getName())) {
                            userRole = role.getName();
                            break;
                        }
                    }

                    if (userRole != null && !userRole.equals("ADMIN")) {
                        // Check if user already exists in backend database
                        boolean existsInBackend = false;
                        switch (userRole.toUpperCase()) {
                            case "ALUNO":
                                existsInBackend = Aluno.find("keycloakId", user.getId()).firstResult() != null;
                                break;
                            case "PROFESSOR":
                                existsInBackend = Professor.find("keycloakId", user.getId()).firstResult() != null;
                                break;
                            case "COORDENADOR":
                                existsInBackend = Coordenador.find("keycloakId", user.getId()).firstResult() != null;
                                break;
                        }

                        if (!existsInBackend) {
                            System.out.println("Syncing user: " + user.getUsername() + " (" + userRole + ")");

                            // Create minimal user data - you'll need to update these manually later
                            switch (userRole.toUpperCase()) {
                                case "ALUNO":
                                    // Create with default course (you'll need to update this)
                                    var firstCourse = Curso.findAll().firstResult();
                                    if (firstCourse != null) {
                                        String nome = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
                                        String matricula = "SYNC_" + user.getUsername(); // Temporary matricula

                                        Aluno aluno = new Aluno(nome, matricula, (Curso) firstCourse, user.getId());

                                        System.out.println("Persisting aluno with data: Nome: " + aluno.nome + ", Matricula: " + aluno.matricula + ", Curso: " + aluno.curso.nome + ", KeycloakId: " + aluno.keycloakId);
                                        aluno.persist();
                                        syncedCount++;
                                    } else {
                                        System.err.println("Cannot sync ALUNO - no courses found in database");
                                        skippedCount++;
                                    }
                                    break;

                                case "PROFESSOR":
                                    String professorNome = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
                                    String professorMatricula = "SYNC_" + user.getUsername(); // Temporary matricula

                                    Professor professor = new Professor(professorNome, professorMatricula, null, user.getId());
                                    System.out.println("Persisting professor with data: Nome: " + professor.nome + ", Registro: " + professor.getRegistro() + ", KeycloakId: " + professor.keycloakId);
                                    professor.persist();
                                    syncedCount++;
                                    break;

                                case "COORDENADOR":
                                    var firstCourseCoord = Curso.findAll().firstResult();
                                    if (firstCourseCoord != null) {
                                        String coordenadorNome = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
                                        String coordenadorMatricula = "SYNC_" + user.getUsername(); // Temporary matricula

                                        Coordenador coordenador = new Coordenador(coordenadorNome, coordenadorMatricula, (Curso) firstCourseCoord, user.getId());
                                        System.out.println("Persisting coordenador with data: Nome: " + coordenador.nome + ", Registro: " + coordenador.getRegistro() + ", Curso: " + coordenador.curso.nome + ", KeycloakId: " + coordenador.keycloakId);
                                        coordenador.persist();
                                        syncedCount++;
                                    } else {
                                        System.err.println("Cannot sync COORDENADOR - no courses found in database");
                                        skippedCount++;
                                    }
                                    break;
                            }
                        } else {
                            System.out.println("User already exists in backend: " + user.getUsername());
                            skippedCount++;
                        }
                    } else {
                        System.out.println("Skipping user with no relevant role: " + user.getUsername());
                        skippedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Error syncing user " + user.getUsername() + ": " + e.getMessage());
                    skippedCount++;
                }
            }

            System.out.println("=== Sync completed: " + syncedCount + " users synced, " + skippedCount + " skipped ===");

        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }
}
