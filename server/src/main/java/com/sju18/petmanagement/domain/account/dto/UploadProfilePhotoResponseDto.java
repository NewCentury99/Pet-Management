package com.sju18.petmanagement.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadProfilePhotoResponseDto {
    private String fileUrl;
    private String message;
}
