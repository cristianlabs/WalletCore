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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class DashboardControllerTest extends AbstractIntegrationTest {

    @Test
    void aggregatesBalanceIncomeExpensesSavingsAndCategoriesForTheCallerOnly() throws Exception {
        String aliceToken = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String bobToken = registerAndLogin("Bob", "bob@example.com", "senha1234");

        String aliceAccount1 = createAccount(aliceToken, "Conta 1", 1000);
        String aliceAccount2 = createAccount(aliceToken, "Conta 2", 500);
        String aliceExpenseCategory = createCategory(aliceToken, "Alimentação", "EXPENSE");
        String aliceIncomeCategory = createCategory(aliceToken, "Salário", "INCOME");

        createTransaction(aliceToken, aliceAccount1, aliceIncomeCategory, "INCOME", 3000);
        createTransaction(aliceToken, aliceAccount1, aliceExpenseCategory, "EXPENSE", 400);
        createTransaction(aliceToken, aliceAccount2, aliceExpenseCategory, "EXPENSE", 150);

        // Bob's own, unrelated data must not leak into Alice's dashboard
        String bobAccount = createAccount(bobToken, "Conta Bob", 99999);
        String bobCategory = createCategory(bobToken, "OutraCoisa", "EXPENSE");
        createTransaction(bobToken, bobAccount, bobCategory, "EXPENSE", 99999);

        MvcResult result = mockMvc.perform(get("/dashboard").header(HttpHeaders.AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andReturn();
        var dashboard = toJsonNode(result);

        assertThat(dashboard.get("totalBalance").asDouble()).isEqualTo(3950.0);
        assertThat(dashboard.get("totalIncome").asDouble()).isEqualTo(3000.0);
        assertThat(dashboard.get("totalExpenses").asDouble()).isEqualTo(550.0);
        assertThat(dashboard.get("savings").asDouble()).isEqualTo(2450.0);
        assertThat(dashboard.get("expensesByCategory")).hasSize(1);
        assertThat(dashboard.get("expensesByCategory").get(0).get("total").asDouble()).isEqualTo(550.0);
    }

    @Test
    void newUserWithNoDataGetsAllZerosNotAnError() throws Exception {
        String token = registerAndLogin("Novo", "novo@example.com", "senha1234");

        MvcResult result = mockMvc.perform(get("/dashboard").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        var dashboard = toJsonNode(result);

        assertThat(dashboard.get("totalBalance").asDouble()).isEqualTo(0.0);
        assertThat(dashboard.get("totalIncome").asDouble()).isEqualTo(0.0);
        assertThat(dashboard.get("totalExpenses").asDouble()).isEqualTo(0.0);
        assertThat(dashboard.get("expensesByCategory")).isEmpty();
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

    protected void createTransaction(String token, String accountId, String categoryId, String type, double amount) throws Exception {
        mockMvc.perform(post("/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("accountId", accountId, "categoryId", categoryId, "type", type, "amount", amount))))
                .andExpect(status().isCreated());
    }
}
