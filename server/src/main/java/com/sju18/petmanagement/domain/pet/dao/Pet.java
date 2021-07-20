package com.sju18.petmanagement.domain.pet.dao;

import com.sju18.petmanagement.domain.pet.dto.PetInfoUpdateRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long id;

    @Column(unique = true)
    private String username;
    private String name;
    private String species;
    private String breed;
    private String birth;
    private String gender;
    private String feed_interval;
    private String memo;
    private String photo_url;

    @Builder
    public Pet(String username, String name, String species, String breed, String birth, String gender, String feed_interval, String memo, String photo_url) {
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

    public Long update(PetInfoUpdateRequestDto requestDto) {
        this.name = requestDto.getName();
        this.species = requestDto.getSpecies();
        this.breed = requestDto.getBreed();
        this.birth = requestDto.getBirth();
        this.gender = requestDto.getGender();
        this.feed_interval = requestDto.getFeed_interval();
        this.memo = requestDto.getMemo();
        this.photo_url = requestDto.getPhoto_url();

        return this.id;
    }
}