package com.sju18.petmanagement.domain.pet.api;

import com.sju18.petmanagement.domain.pet.application.PetService;
import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class PetController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getPetMessageSource();
    private final PetService petServ;

    // CREATE
    @PostMapping("/api/pet/create")
    public ResponseEntity<?> createPet(Authentication auth, @Valid @RequestBody CreatePetReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            petServ.createPet(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreatePetResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.pet.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreatePetResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/pet/fetch")
    public ResponseEntity<?> fetchPet(Authentication auth, @Valid @RequestBody FetchPetReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<Pet> petList;

        try {
            if (reqDto.getId() != null) {
                petList = new ArrayList<>();
                petList.add(petServ.fetchPetById(reqDto.getId()));
            } else {
                petList = petServ.fetchPetList(auth);
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPetResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.pet.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchPetResDto(dtoMetadata, petList));
    }

    @PostMapping("/api/pet/photo/fetch")
    public ResponseEntity<?> fetchPetPhoto(Authentication auth, @Valid @RequestBody FetchPetPhotoReqDto reqDto) {
        DtoMetadata dtoMetadata;
        byte[] fileBinData;
        try {
            fileBinData = petServ.fetchPetPhoto(auth, reqDto.getId());
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPetPhotoResDto(dtoMetadata));
        }
        return ResponseEntity.ok(fileBinData);
    }

    // UPDATE
    @PostMapping("/api/pet/update")
    public ResponseEntity<?> updatePet(Authentication auth, @Valid @RequestBody UpdatePetReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            petServ.updatePet(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdatePetResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.pet.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdatePetResDto(dtoMetadata));
    }

    @PostMapping("/api/pet/photo/update")
    public ResponseEntity<?> updatePetPhoto(Authentication auth, @ModelAttribute UpdatePetPhotoReqDto reqDto) {
        DtoMetadata dtoMetadata;
        String fileUrl;
        try {
            fileUrl = petServ.updatePetPhoto(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdatePetPhotoResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.petPhoto.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdatePetPhotoResDto(dtoMetadata, fileUrl));
    }

    // DELETE
    @PostMapping("/api/pet/delete")
    public ResponseEntity<?> deletePet(Authentication auth, @Valid @RequestBody DeletePetReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            petServ.deletePet(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeletePetResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.pet.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeletePetResDto(dtoMetadata));
    }
}