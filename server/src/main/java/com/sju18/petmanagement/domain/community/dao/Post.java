package com.sju18.petmanagement.domain.community.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.pet.dao.Pet;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account author;

    @ManyToOne(targetEntity = Pet.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Column(nullable = false)
    private String contents;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(nullable = false)
    private Boolean edited;

    @Column(name = "tag_list")
    private String serializedHashTags;

    @Column
    private String disclosure;
    private Double geoTagLat;
    private Double geoTagLong;
    private String attachedImagesJsonArray;
}
