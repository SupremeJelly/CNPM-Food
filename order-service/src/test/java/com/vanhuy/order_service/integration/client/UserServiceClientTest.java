package com.vanhuy.order_service.integration.client;

import com.vanhuy.order_service.client.UserServiceClient;
import com.vanhuy.order_service.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class UserServiceClientTest {

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void testGetUserById_Success() {
        // Tạo đối tượng giả
        // UserDTO mockUser = new UserDTO();
        // mockUser.setUserId(1);
        // mockUser.setUsername("Nguyen Van A");
        // mockUser.setEmail("nva@example.com");
        // mockUser.setAddress("Hanoi");
        UserDTO mockUser = UserDTO.builder()
            .userId(1)
            .username("Nguyen Van A")
            .email("nva@example.com")
            .address("Hanoi")
            .build();

        // Khi gọi getUserById(1), trả về mockUser
        when(userServiceClient.getUserById(1)).thenReturn(mockUser);

        // Gọi hàm thật (thực ra gọi mock)
        UserDTO result = userServiceClient.getUserById(1);

        // Kiểm tra kết quả
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("Nguyen Van A");
        assertThat(result.getEmail()).isEqualTo("nva@example.com");
        assertThat(result.getAddress()).isEqualTo("Hanoi");

        // Đảm bảo mock được gọi đúng 1 lần
        verify(userServiceClient, times(1)).getUserById(1);
    }

    @Test
    void testGetUserById_NotFound() {
        // Khi gọi với ID không tồn tại, trả về null
        when(userServiceClient.getUserById(999)).thenReturn(null);

        UserDTO result = userServiceClient.getUserById(999);

        assertThat(result).isNull();
        verify(userServiceClient, times(1)).getUserById(999);
    }
}
