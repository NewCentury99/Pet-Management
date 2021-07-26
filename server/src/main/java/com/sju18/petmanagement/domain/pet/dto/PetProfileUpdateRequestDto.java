package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PetProfileUpdateRequestDto {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String birth;
    private Boolean gender;
    private String message;
    private String photo_url;
}