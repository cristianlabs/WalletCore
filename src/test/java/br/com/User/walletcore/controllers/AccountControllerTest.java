package br.com.User.walletcore.controllers;

import br.com.User.walletcore.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AccountControllerTest extends AbstractIntegrationTest {

    @Test
    void createRejectsNegativeBalance() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");

        mockMvc.perform(post("/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Conta", "balance", -100, "type", "CHECKING"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAllowsZeroBalance() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");

        mockMvc.perform(post("/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Conta", "balance", 0, "type", "CHECKING"))))
                .andExpect(status().isCreated());
    }

    @Test
    void ownerCanReadUpdateAndDeleteTheirOwnAccount() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 1000);

        mockMvc.perform(get("/accounts/" + accountId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conta"));

        mockMvc.perform(put("/accounts/" + accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Conta Renomeada", "balance", 1000, "type", "CHECKING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conta Renomeada"));

        mockMvc.perform(delete("/accounts/" + accountId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/accounts/" + accountId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherUserCannotReadUpdateOrDeleteSomeoneElsesAccount() throws Exception {
        String ownerToken = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String attackerToken = registerAndLogin("Bob", "bob@example.com", "senha1234");
        String accountId = createAccount(ownerToken, "Conta da Alice", 1000);

        mockMvc.perform(get("/accounts/" + accountId).header(HttpHeaders.AUTHORIZATION, bearer(attackerToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/accounts/" + accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attackerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Hackeada", "balance", 0, "type", "CHECKING"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/accounts/" + accountId).header(HttpHeaders.AUTHORIZATION, bearer(attackerToken)))
                .andExpect(status().isNotFound());

        // account must be untouched
        mockMvc.perform(get("/accounts/" + accountId).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conta da Alice"));
    }

    @Test
    void listOnlyReturnsAccountsOwnedByTheCaller() throws Exception {
        String aliceToken = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String bobToken = registerAndLogin("Bob", "bob@example.com", "senha1234");
        createAccount(aliceToken, "Conta Alice", 100);
        createAccount(bobToken, "Conta Bob", 200);

        MvcResult result = mockMvc.perform(get("/accounts").header(HttpHeaders.AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andReturn();

        var accounts = toJsonNode(result);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).get("name").asText()).isEqualTo("Conta Alice");
    }

    protected String createAccount(String token, String name, double balance) throws Exception {
        MvcResult result = mockMvc.perform(post("/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", name, "balance", balance, "type", "CHECKING"))))
                .andExpect(status().isCreated())
                .andReturn();
        return toJsonNode(result).get("id").asText();
    }
}
