package com.vanhuy.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class UserDTO {
    private Integer userId;
    private String username;
    private String email;
    private String address;
}

// public class UserDTO {
//     private Integer id;
//     private String name;
//     private String email;
//     private String phone;
// }
