package com.sju18.PetManagement.domain.account.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequestDTO {
    private String username;
    private String email;
    private String name;
    private String phone;
    private String photo;
}
