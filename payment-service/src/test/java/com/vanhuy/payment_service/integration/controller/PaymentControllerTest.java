package com.vanhuy.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanhuy.payment_service.dto.PaymentRequest;
import com.vanhuy.payment_service.model.Payment;
import com.vanhuy.payment_service.model.Payment.PaymentMethod;
import com.vanhuy.payment_service.model.Payment.PaymentStatus;
import com.vanhuy.payment_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    })
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePayment_ShouldReturnCreatedPayment() throws Exception {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(100);
        payment.setAmount(new BigDecimal("50.00"));
        payment.setStatus(PaymentStatus.PAID);

        Mockito.when(paymentService.createPayment(any(Payment.class))).thenReturn(payment);

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(100);
        request.setAmount(new BigDecimal("50.00"));
        request.setMethod(PaymentMethod.CASH_ON_DELIVERY);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(100));
    }

    @Test
    void testGetAllPayments_ShouldReturnList() throws Exception {
        Payment p = new Payment();
        p.setId(1L);
        p.setOrderId(200);

        Mockito.when(paymentService.getAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(200));
    }

    @Test
    void testGetPaymentById_ShouldReturnPayment() throws Exception {
        Payment p = new Payment();
        p.setId(5L);
        p.setOrderId(123);

        Mockito.when(paymentService.getById(5L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/v1/payments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(123));
    }

    @Test
    void testUpdateStatus_ShouldReturnUpdatedPayment() throws Exception {
        Payment p = new Payment();
        p.setId(10L);
        p.setOrderId(100);
        p.setStatus(PaymentStatus.FAILED);

        Mockito.when(paymentService.updateStatus(eq(10L), eq(PaymentStatus.FAILED))).thenReturn(p);

        mockMvc.perform(put("/api/v1/payments/10/status")
                        .param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }
}
