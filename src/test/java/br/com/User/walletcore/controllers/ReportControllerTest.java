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
class ReportControllerTest extends AbstractIntegrationTest {

    @Test
    void monthlyReportIsScopedToTheRequestedMonthOnly() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 10000);
        String expenseCategoryId = createCategory(token, "Alimentação", "EXPENSE");
        String incomeCategoryId = createCategory(token, "Salário", "INCOME");

        createTransaction(token, accountId, incomeCategoryId, "INCOME", 3000, "2026-01-15T12:00:00Z");
        createTransaction(token, accountId, expenseCategoryId, "EXPENSE", 500, "2026-01-20T12:00:00Z");
        createTransaction(token, accountId, incomeCategoryId, "INCOME", 3200, "2026-02-10T12:00:00Z");

        MvcResult january = mockMvc.perform(get("/reports/monthly?year=2026&month=1").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        var body = toJsonNode(january);
        assertThat(body.get("totalIncome").asDouble()).isEqualTo(3000.0);
        assertThat(body.get("totalExpenses").asDouble()).isEqualTo(500.0);
        assertThat(body.get("savings").asDouble()).isEqualTo(2500.0);
    }

    @Test
    void yearlyReportBreaksDownByUtcMonthAtTheExactBoundary() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 10000);
        String incomeCategoryId = createCategory(token, "Salário", "INCOME");

        // exactly midnight UTC on Jan 1st must count as January, not December
        createTransaction(token, accountId, incomeCategoryId, "INCOME", 50, "2026-01-01T00:00:00Z");
        // one second before midnight on Dec 31st (previous year) must count as December of that year
        createTransaction(token, accountId, incomeCategoryId, "INCOME", 25, "2025-12-31T23:59:59Z");
        createTransaction(token, accountId, incomeCategoryId, "INCOME", 3000, "2026-01-15T12:00:00Z");

        MvcResult result = mockMvc.perform(get("/reports/year?year=2026").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        var body = toJsonNode(result);
        assertThat(body.get("totalIncome").asDouble()).isEqualTo(3050.0);
        assertThat(body.get("months").get(0).get("totalIncome").asDouble()).isEqualTo(3050.0); // January
        assertThat(body.get("months").get(1).get("totalIncome").asDouble()).isEqualTo(0.0); // February

        MvcResult previousYear = mockMvc.perform(get("/reports/year?year=2025").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        var previousYearBody = toJsonNode(previousYear);
        assertThat(previousYearBody.get("totalIncome").asDouble()).isEqualTo(25.0);
        assertThat(previousYearBody.get("months").get(11).get("totalIncome").asDouble()).isEqualTo(25.0); // December
    }

    @Test
    void categoryReportCanBeFilteredByDateRange() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountId = createAccount(token, "Conta", 10000);
        String expenseCategoryId = createCategory(token, "Alimentação", "EXPENSE");

        createTransaction(token, accountId, expenseCategoryId, "EXPENSE", 100, "2026-01-10T12:00:00Z");
        createTransaction(token, accountId, expenseCategoryId, "EXPENSE", 200, "2026-02-10T12:00:00Z");

        MvcResult result = mockMvc.perform(get("/reports/category?from=2026-02-01&to=2026-02-28")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        var body = toJsonNode(result);
        assertThat(body).hasSize(1);
        assertThat(body.get(0).get("total").asDouble()).isEqualTo(200.0);
    }

    // Regression test: a missing required query param must return 400, not the misleading 401
    // that used to surface once the exception fell through to the container's /error forward.
    @Test
    void missingRequiredParameterReturnsBadRequestNotUnauthorized() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");

        mockMvc.perform(get("/reports/monthly?month=1").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/reports/monthly?year=2026").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/reports/year").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void outOfRangeOrInvalidYearAndMonthAreRejectedCleanly() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");

        mockMvc.perform(get("/reports/monthly?year=2026&month=13").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/reports/year?year=999999999").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/reports/year?year=-5").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/reports/monthly?year=abc&month=1").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
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

    protected void createTransaction(String token, String accountId, String categoryId, String type, double amount, String occurredAt) throws Exception {
        mockMvc.perform(post("/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "accountId", accountId,
                                "categoryId", categoryId,
                                "type", type,
                                "amount", amount,
                                "occurredAt", occurredAt
                        ))))
                .andExpect(status().isCreated());
    }
}
