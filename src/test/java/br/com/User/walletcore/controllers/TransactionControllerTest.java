package br.com.User.walletcore.controllers;

import br.com.User.walletcore.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class TransactionControllerTest extends AbstractIntegrationTest {

    @Test
    void expenseReducesAccountBalanceAndIncomeIncreasesIt() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 1000);
        String expenseCategoryId = createCategory(token, "Alimentação", "EXPENSE");
        String incomeCategoryId = createCategory(token, "Salário", "INCOME");

        createTransaction(token, accountId, expenseCategoryId, "EXPENSE", 150.75, null)
                .andExpect(status().isCreated());
        assertBalance(token, accountId, 849.25);

        createTransaction(token, accountId, incomeCategoryId, "INCOME", 200, null)
                .andExpect(status().isCreated());
        assertBalance(token, accountId, 1049.25);
    }

    @Test
    void expenseThatWouldOverdrawTheAccountIsRejectedAndBalanceIsUnchanged() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 100);
        String categoryId = createCategory(token, "Alimentação", "EXPENSE");

        createTransaction(token, accountId, categoryId, "EXPENSE", 150, null)
                .andExpect(status().isConflict());

        assertBalance(token, accountId, 100.0);
    }

    @Test
    void expenseThatLeavesExactlyZeroIsAllowed() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 100);
        String categoryId = createCategory(token, "Alimentação", "EXPENSE");

        createTransaction(token, accountId, categoryId, "EXPENSE", 100, null)
                .andExpect(status().isCreated());

        assertBalance(token, accountId, 0.0);
    }

    @Test
    void nonPositiveAmountsAreRejected() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 100);
        String categoryId = createCategory(token, "Alimentação", "EXPENSE");

        createTransaction(token, accountId, categoryId, "EXPENSE", 0, null).andExpect(status().isBadRequest());
        createTransaction(token, accountId, categoryId, "EXPENSE", -10, null).andExpect(status().isBadRequest());
    }

    @Test
    void categoryTypeMustMatchTransactionType() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 100);
        String expenseCategoryId = createCategory(token, "Alimentação", "EXPENSE");

        createTransaction(token, accountId, expenseCategoryId, "INCOME", 50, null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannotUseAnotherUsersAccountOrCategory() throws Exception {
        String ownerToken = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String attackerToken = registerAndLogin("Bob", "bob@example.com", "senha1234");

        String ownerAccountId = createAccount(ownerToken, "Conta Alice", 1000);
        String ownerCategoryId = createCategory(ownerToken, "Alimentação", "EXPENSE");
        String attackerAccountId = createAccount(attackerToken, "Conta Bob", 1000);
        String attackerCategoryId = createCategory(attackerToken, "Transporte", "EXPENSE");

        createTransaction(attackerToken, ownerAccountId, attackerCategoryId, "EXPENSE", 10, null)
                .andExpect(status().isNotFound());
        createTransaction(attackerToken, attackerAccountId, ownerCategoryId, "EXPENSE", 10, null)
                .andExpect(status().isNotFound());
    }

    @Test
    void updatingATransactionMovesTheBalanceEffectBetweenAccounts() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountA = createAccount(token, "Conta A", 1000);
        String accountB = createAccount(token, "Conta B", 1000);
        String categoryId = createCategory(token, "Alimentação", "EXPENSE");

        MvcResult created = createTransaction(token, accountA, categoryId, "EXPENSE", 100, null)
                .andExpect(status().isCreated())
                .andReturn();
        String transactionId = toJsonNode(created).get("id").asText();
        assertBalance(token, accountA, 900.0);

        Map<String, Object> body = new HashMap<>();
        body.put("accountId", accountB);
        body.put("categoryId", categoryId);
        body.put("type", "EXPENSE");
        body.put("amount", 100);
        mockMvc.perform(put("/transactions/" + transactionId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk());

        assertBalance(token, accountA, 1000.0);
        assertBalance(token, accountB, 900.0);
    }

    @Test
    void deletingATransactionReversesItsBalanceEffect() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 1000);
        String categoryId = createCategory(token, "Alimentação", "EXPENSE");

        MvcResult created = createTransaction(token, accountId, categoryId, "EXPENSE", 300, null)
                .andExpect(status().isCreated())
                .andReturn();
        String transactionId = toJsonNode(created).get("id").asText();
        assertBalance(token, accountId, 700.0);

        mockMvc.perform(delete("/transactions/" + transactionId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertBalance(token, accountId, 1000.0);
    }

    @Test
    void listIsPaginatedWithADefaultAndACappedMaxSize() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 100000);
        String categoryId = createCategory(token, "Alimentação", "EXPENSE");

        for (int i = 0; i < 25; i++) {
            createTransaction(token, accountId, categoryId, "EXPENSE", 1, null).andExpect(status().isCreated());
        }

        MvcResult defaultPage = mockMvc.perform(get("/transactions").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        var defaultBody = toJsonNode(defaultPage);
        assertThat(defaultBody.get("content")).hasSize(20);
        assertThat(defaultBody.get("totalElements").asInt()).isEqualTo(25);

        MvcResult oversizedPage = mockMvc.perform(get("/transactions?size=1000").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(toJsonNode(oversizedPage).get("size").asInt()).isEqualTo(100);
    }

    private void assertBalance(String token, String accountId, double expected) throws Exception {
        MvcResult result = mockMvc.perform(get("/accounts/" + accountId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(toJsonNode(result).get("balance").asDouble()).isEqualTo(expected);
    }

    private ResultActions createTransaction(
            String token, String accountId, String categoryId, String type, double amount, String occurredAt
    ) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("accountId", accountId);
        body.put("categoryId", categoryId);
        body.put("type", type);
        body.put("amount", amount);
        if (occurredAt != null) {
            body.put("occurredAt", occurredAt);
        }
        return mockMvc.perform(post("/transactions")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
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
