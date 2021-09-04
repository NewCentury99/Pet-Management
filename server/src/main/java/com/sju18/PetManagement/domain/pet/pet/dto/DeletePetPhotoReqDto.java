package com.sju18.petmanagement.domain.pet.pet.dto;

import lombok.Data;
import javax.validation.constraints.PositiveOrZero;

@Data
public class DeletePetPhotoReqDto {
    @PositiveOrZero(message = "valid.pet.id.notNegative")
    private Long id;
}
