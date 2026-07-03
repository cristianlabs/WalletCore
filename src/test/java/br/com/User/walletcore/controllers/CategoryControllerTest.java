package br.com.User.walletcore.controllers;

import br.com.User.walletcore.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class CategoryControllerTest extends AbstractIntegrationTest {

    @Test
    void duplicateNameForTheSameOwnerIsRejectedCaseInsensitively() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        createCategory(token, "Alimentação", "EXPENSE");

        mockMvc.perform(post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "alimentação", "type", "EXPENSE"))))
                .andExpect(status().isConflict());
    }

    @Test
    void differentOwnersCanUseTheSameCategoryName() throws Exception {
        String aliceToken = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String bobToken = registerAndLogin("Bob", "bob@example.com", "senha1234");
        createCategory(aliceToken, "Alimentação", "EXPENSE");

        mockMvc.perform(post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Alimentação", "type", "EXPENSE"))))
                .andExpect(status().isCreated());
    }

    @Test
    void anotherUserCannotReadOrDeleteSomeoneElsesCategory() throws Exception {
        String ownerToken = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String attackerToken = registerAndLogin("Bob", "bob@example.com", "senha1234");
        String categoryId = createCategory(ownerToken, "Alimentação", "EXPENSE");

        mockMvc.perform(get("/categories/" + categoryId).header(HttpHeaders.AUTHORIZATION, bearer(attackerToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/categories/" + categoryId).header(HttpHeaders.AUTHORIZATION, bearer(attackerToken)))
                .andExpect(status().isNotFound());
    }

    protected String createCategory(String token, String name, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", name, "type", type))))
                .andExpect(status().isCreated())
                .andReturn();
        return toJsonNode(result).get("id").asText();
    }
}
