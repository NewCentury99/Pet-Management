package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.ProfileLookupRequestDto;
import com.sju18.petmanagement.domain.account.dto.ProfileLookupResponseDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.InputStream;

@RestController
@RequiredArgsConstructor
public class ProfileLookupController {
    private static final Logger logger = LogManager.getLogger();

    final AccountRepository accountRepository;

    @PostMapping("/api/account/profilelookup")
    public ResponseEntity<?> lookupAccountProfile(Authentication authentication, @RequestBody ProfileLookupRequestDto profilelookupRequestDto) {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();

        // 해당 사용자의 세부정보 조회 및 반환
        try {
            Account currentUserProfile = accountRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new UsernameNotFoundException(currentUserName));
            return ResponseEntity.ok(new ProfileLookupResponseDto(
                    "Account profile lookup success",
                    currentUserProfile.getUsername(),
                    currentUserProfile.getEmail(),
                    currentUserProfile.getNickname(),
                    currentUserProfile.getPhone(),
                    currentUserProfile.getPhotoUrl(),
                    currentUserProfile.getMarketing(),
                    currentUserProfile.getUserMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ProfileLookupResponseDto(e.getMessage()));
        }
    }

    @GetMapping("/api/account/fetchprofilephoto")
    public ResponseEntity<?> fetchProfilePhoto(Authentication authentication) {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();

        // 해당 사용자의 프로필 사진 경로 조회 및 이미지 파일 반환
        try {
            Account currentUserProfile = accountRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new UsernameNotFoundException(currentUserName));
            InputStream imageStream = new FileInputStream(currentUserProfile.getPhotoUrl());
            byte[] imageByteArray = IOUtil.toByteArray(imageStream);
            imageStream.close();
            return ResponseEntity.ok(imageByteArray);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ProfileLookupResponseDto(e.getMessage()));
        }
    }
}
