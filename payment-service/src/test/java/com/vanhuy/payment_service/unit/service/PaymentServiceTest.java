package com.vanhuy.payment_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vanhuy.payment_service.client.OrderClient;
import com.vanhuy.payment_service.client.RestaurantClient;
import com.vanhuy.payment_service.model.Payment;
import com.vanhuy.payment_service.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository repo;

    @Mock
    private OrderClient orderClient;

    @Mock
    private RestaurantClient restaurantClient;

    @InjectMocks
    private PaymentService service;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(100);
        payment.setAmount(new BigDecimal("50.00"));
        payment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreatePayment_ShouldSetStatusPaidAndCallOrderClient() {
        when(repo.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = service.createPayment(payment);

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.PAID);
        verify(orderClient).updatePaymentStatus(eq(100), eq("PAID"));
        verify(repo).save(any(Payment.class));
    }

    @Test
    void testGetById_ShouldReturnPayment() {
        when(repo.findById(1L)).thenReturn(Optional.of(payment));

        Optional<Payment> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(100L);
        verify(repo).findById(1L);
    }

    @Test
    void testUpdateStatus_ShouldUpdatePaymentAndCallOrderService() {
        when(repo.findById(1L)).thenReturn(Optional.of(payment));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment result = service.updateStatus(1L, Payment.PaymentStatus.FAILED);

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        verify(orderClient).updatePaymentStatus(100, "FAILED");
        verify(repo, times(1)).save(any(Payment.class));
    }

    @Test
    void testGetAll_ShouldReturnListOfPayments() {
        when(repo.findAll()).thenReturn(List.of(payment));

        List<Payment> payments = service.getAll();

        assertThat(payments).hasSize(1);
        verify(repo).findAll();
    }

    @Test
    void testGetByOrderId_ShouldReturnPayments() {
        when(repo.findByOrderId(100L)).thenReturn(List.of(payment));

        List<Payment> payments = service.getByOrderId(100L);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getOrderId()).isEqualTo(100L);
        verify(repo).findByOrderId(100L);
    }
}
