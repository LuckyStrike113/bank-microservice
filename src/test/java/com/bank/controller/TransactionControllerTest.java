package com.bank.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.entity.ExpenseCategory;
import com.bank.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for {@link TransactionController}.
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    /**
     * Tests successful transaction creation with valid data.
     */
    @Test
    void createTransaction_validRequest_returnsOk() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountFrom("1234567890");
        request.setAccountTo("0987654321");
        request.setCurrencyShortname("KZT");
        request.setSum(new BigDecimal("10000.00"));
        request.setExpenseCategory(ExpenseCategory.PRODUCT);
        request.setDatetime(OffsetDateTime.now().minusHours(1));

        TransactionResponse response = new TransactionResponse();
        response.setAccountFrom("1234567890");
        response.setAccountTo("0987654321");
        response.setCurrencyShortname("KZT");
        response.setSum(new BigDecimal("10000.00"));
        response.setExpenseCategory(ExpenseCategory.PRODUCT);
        response.setDatetime(request.getDatetime());
        response.setLimitExceeded(false);
        response.setLimitSum(new BigDecimal("1000.00"));
        response.setLimitDatetime(OffsetDateTime.now().minusDays(1));
        response.setLimitCurrencyShortname("USD");

        when(transactionService.processTransaction(any(TransactionRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountFrom").value("1234567890"))
            .andExpect(jsonPath("$.accountTo").value("0987654321"))
            .andExpect(jsonPath("$.currencyShortname").value("KZT"))
            .andExpect(jsonPath("$.sum").value(10000.00))
            .andExpect(jsonPath("$.expenseCategory").value("PRODUCT"))
            .andExpect(jsonPath("$.limitExceeded").value(false))
            .andExpect(jsonPath("$.limitCurrencyShortname").value("USD"));
    }

    /**
     * Tests transaction creation with invalid account number, expecting a bad request response.
     */
    @Test
    void createTransaction_invalidAccountFrom_returnsBadRequest() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountFrom("123"); // неверный формат
        request.setAccountTo("0987654321");
        request.setCurrencyShortname("KZT");
        request.setSum(new BigDecimal("10000.00"));
        request.setExpenseCategory(ExpenseCategory.PRODUCT);
        request.setDatetime(OffsetDateTime.now().minusHours(1));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.accountFrom").value("Account from must be a 10-digit number"));
    }

    /**
     * Tests transaction creation with future datetime, expecting a bad request response.
     */
    @Test
    void createTransaction_futureDatetime_returnsBadRequest() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountFrom("1234567890");
        request.setAccountTo("0987654321");
        request.setCurrencyShortname("KZT");
        request.setSum(new BigDecimal("10000.00"));
        request.setExpenseCategory(ExpenseCategory.PRODUCT);
        request.setDatetime(OffsetDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.datetime").value("Datetime must be in the past or present"));
    }
}