package com.nexastudio.project;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexastudio.auth.JwtUtil;
import com.nexastudio.project.dto.CreateProjectRequest;
import com.nexastudio.user.UserEntity;
import com.nexastudio.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String authToken;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = UserEntity.builder()
                .username("project-test-" + UUID.randomUUID())
                .passwordHash(passwordEncoder.encode("password123"))
                .build();
        testUser = userRepository.save(testUser);

        // Generate JWT token
        authToken = "Bearer " + jwtUtil.generateToken(testUser, testUser.getId());
    }

    @Test
    void shouldCreateProject() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("My Test Project")
                .description("A test project")
                .type("NEXTJS")
                .build();

        mockMvc.perform(post("/projects")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("My Test Project"))
                .andExpect(jsonPath("$.data.type").value("NEXTJS"));
    }

    @Test
    void shouldGetProjectsList() throws Exception {
        // Create a project first
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("List Test Project")
                .build();

        mockMvc.perform(post("/projects")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Get projects list
        mockMvc.perform(get("/projects")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectEmptyProjectName() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/projects")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
