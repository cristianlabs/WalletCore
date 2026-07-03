package br.com.User.walletcore.controllers;

import br.com.User.walletcore.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    void registerCreatesUserWithoutExposingPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Alice", "email", "alice@example.com", "password", "senha1234"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerNormalizesEmailAndBlocksDuplicates() throws Exception {
        register("Alice", "  Alice@Example.com  ", "senha1234");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Alice2", "email", "alice@example.com", "password", "outrasenha"))))
                .andExpect(status().isConflict());

        // login with different case still resolves to the same, normalized account
        String token = login("ALICE@EXAMPLE.COM", "senha1234");
        assertThat(token).isNotBlank();
    }

    @Test
    void registerRejectsInvalidFields() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "", "email", "not-an-email", "password", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        register("Alice", "alice@example.com", "senha1234");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "alice@example.com", "password", "errada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsUnknownEmailWithoutLeakingExistence() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "nobody@example.com", "password", "senha1234"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenReturnsCleanJson401() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpointWithGarbageTokenReturns401NotServerError() throws Exception {
        mockMvc.perform(get("/accounts").header(HttpHeaders.AUTHORIZATION, bearer("not-a-real-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rootEndpointIsPublic() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("WalletCore API"))
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("version");
    }
}
