package com.sju18.petmanagement.domain.pet.dao;

import com.sju18.petmanagement.domain.pet.dto.PetProfileFetchResponseDto;
import com.sju18.petmanagement.domain.pet.dto.PetProfileUpdateRequestDto;
import com.sju18.petmanagement.domain.pet.dto.PetProfileUpdateResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long id;

    @Column
    private String username;
    private String name;
    private String species;
    private String breed;
    private LocalDate birth;
    private Boolean gender;
    private String feed_interval;
    private String memo;
    private String photo_url;

    @Builder
    public Pet(String username, String name, String species, String breed, LocalDate birth, Boolean gender, String feed_interval, String memo, String photo_url) {
        this.username = username;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birth = birth;
        this.gender = gender;
        this.feed_interval = feed_interval;
        this.memo = memo;
        this.photo_url = photo_url;
    }

    public void update(PetProfileUpdateRequestDto requestDto) {
        this.name = requestDto.getName();
        this.species = requestDto.getSpecies();
        this.breed = requestDto.getBreed();
        this.birth = requestDto.getBirth();
        this.gender = requestDto.getGender();
        this.feed_interval = requestDto.getFeed_interval();
        this.memo = requestDto.getMemo();
        this.photo_url = requestDto.getPhoto_url();
    }
}