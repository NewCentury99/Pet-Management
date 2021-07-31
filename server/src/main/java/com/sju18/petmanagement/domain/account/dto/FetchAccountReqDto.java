package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchAccountReqDto {
    @PositiveOrZero(message = "valid.id.notNegative")
    private Long id;
    private String username;
}
