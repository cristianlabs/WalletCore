package br.com.User.walletcore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Deliberately not @Transactional: concurrent worker threads don't join the test method's
// transaction (it's bound to the calling thread only), so rollback wouldn't clean up their
// writes anyway. Truncate manually instead.
class ConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE transactions, categories, accounts, users CASCADE");
    }

    @Test
    void onlyOneOfManyConcurrentRegistrationsWithTheSameEmailSucceeds() throws Exception {
        int attempts = 8;
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                tasks.add(() -> mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of("name", "Racer", "email", "race@example.com", "password", "senha1234"))))
                        .andReturn().getResponse().getStatus());
            }

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> future : executor.invokeAll(tasks, 60, TimeUnit.SECONDS)) {
                statuses.add(future.get());
            }

            assertThat(statuses).allMatch(status -> status == 201 || status == 409);
            assertThat(statuses).filteredOn(status -> status == 201).hasSize(1);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void concurrentTransfersBetweenTheSameTwoAccountsInOppositeDirectionsNeverFailWithServerError() throws Exception {
        String token = registerAndLogin("Alice", "alice@example.com", "senha1234");
        String accountA = createAccount(token, "Deadlock A", 100000);
        String accountB = createAccount(token, "Deadlock B", 100000);
        String categoryId = createCategory(token, "DeadlockCat");

        String tx1 = createTransaction(token, accountA, categoryId, 10);
        String tx2 = createTransaction(token, accountB, categoryId, 10);

        int rounds = 4;
        ExecutorService executor = Executors.newFixedThreadPool(rounds * 2);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 0; i < rounds; i++) {
                tasks.add(() -> moveTransaction(token, tx1, accountB, categoryId));
                tasks.add(() -> moveTransaction(token, tx2, accountA, categoryId));
            }

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> future : executor.invokeAll(tasks, 60, TimeUnit.SECONDS)) {
                statuses.add(future.get());
            }

            assertThat(statuses).allMatch(status -> status < 500);
        } finally {
            executor.shutdown();
        }
    }

    private int moveTransaction(String token, String transactionId, String targetAccountId, String categoryId) throws Exception {
        return mockMvc.perform(put("/transactions/" + transactionId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("accountId", targetAccountId, "categoryId", categoryId, "type", "EXPENSE", "amount", 10))))
                .andReturn().getResponse().getStatus();
    }

    private String createAccount(String token, String name, double balance) throws Exception {
        MvcResult result = mockMvc.perform(post("/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", name, "balance", balance, "type", "CHECKING"))))
                .andExpect(status().isCreated())
                .andReturn();
        return toJsonNode(result).get("id").asText();
    }

    private String createCategory(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", name, "type", "EXPENSE"))))
                .andExpect(status().isCreated())
                .andReturn();
        return toJsonNode(result).get("id").asText();
    }

    private String createTransaction(String token, String accountId, String categoryId, double amount) throws Exception {
        MvcResult result = mockMvc.perform(post("/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("accountId", accountId, "categoryId", categoryId, "type", "EXPENSE", "amount", amount))))
                .andExpect(status().isCreated())
                .andReturn();
        return toJsonNode(result).get("id").asText();
    }
}
