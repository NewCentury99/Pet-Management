package com.sju18.petmanagement.global.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtil;
import org.aspectj.util.FileUtil;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RequiredArgsConstructor
@Service
public class FileService {
    private final MessageSource msgSrc = MessageConfig.getStorageMessageSource();
    private final String storageRootPath = "C:\\Users\\fchop\\Development\\tmp\\Pet-Management\\storage";
    private final Integer MEDIA_FILE = 1;
    private final Integer GENERAL_FILE = 2;

    // 파일 메타데이터 목록(stringify 된 JSON)을 이용하여 파일 읽기
    public byte[] readFileFromFileMetadataListJson(String fileMetadataListJson, Integer fileIndex) throws IOException {
        Type collectionType = new TypeToken<List<FileMetadata>>(){}.getType();
        List<FileMetadata> fileMetadataList = new Gson()
                .fromJson(fileMetadataListJson, collectionType);
        String fileUrl = fileMetadataList.get(fileIndex).getUrl();
        InputStream mediaStream = new FileInputStream(fileUrl);
        byte[] fileBinData = IOUtil.toByteArray(mediaStream);
        mediaStream.close();
        return fileBinData;
    }

    // 특정 사용자 데이터 폴더 경로 조회
    public Path getAccountFileStoragePath(Long accountId) {
        return Paths.get(storageRootPath, "accounts", "account_" + accountId);
    }
    // 특정 반려동물 데이터 폴더 경로 조회
    public Path getPetFileStoragePath(Long ownerAccountId, Long petId) {
        return Paths.get(getAccountFileStoragePath(ownerAccountId).toString(), "pets", "pet_" + petId);
    }
    // 특정 게시물 데이터 폴더 경로 조회
    public Path getPostFileStoragePath(Long postId) {
        return Paths.get(storageRootPath, "community", "posts", "post_" + postId);
    }
    // 특정 게시물 댓글 데이터 폴더 경로 조회 - TODO: 장래 댓글 첨부파일 기능 구현시 같이 구현예정
    // TODO: getCommentFileStoragePath(Long commentId) 구현
    // 특정 장소 리뷰 데이터 폴더 경로 조회
    public Path getReviewFileStoragePath(Long reviewId) {
        return Paths.get(storageRootPath, "map", "reviews", "review_" + reviewId);
    }

    // 사용자 데이터 폴더 생성
    public void createAccountFileStorage(Long accountId) throws Exception {
        Path accountStorage = getAccountFileStoragePath(accountId);
        Files.createDirectories(accountStorage);
        FileUtil.makeNewChildDir(accountStorage.toFile(), "pets");
    }
    // 사용자 데이터 폴더 삭제
    public void deleteAccountFileStorage(Long accountId) throws Exception {
        Path accountStorage = getAccountFileStoragePath(accountId);
        FileUtils.deleteDirectory(accountStorage.toFile());
    }

    // 반려동물 폴더 생성
    public void createPetFileStorage(Long ownerAccountId, Long petId) throws Exception {
        Path petStorage = getPetFileStoragePath(ownerAccountId, petId);
        Files.createDirectories(petStorage);
    }
    // 반려동물 폴더 삭제
    public void deletePetFileStorage(Long ownerAccountId, Long petId) throws Exception {
        Path petStorage = getPetFileStoragePath(ownerAccountId, petId);
        FileUtils.deleteDirectory(petStorage.toFile());
    }

    // 게시물 데이터 폴더 생성
    public void createPostFileStorage(Long postId) throws Exception {
        Path postAttachedFileStorage = getPostFileStoragePath(postId);
        Files.createDirectories(postAttachedFileStorage);
        FileUtil.makeNewChildDir(postAttachedFileStorage.toFile(), "media");
        FileUtil.makeNewChildDir(postAttachedFileStorage.toFile(), "general");
        FileUtil.makeNewChildDir(postAttachedFileStorage.toFile(), "comments");
    }
    // 게시물 데이터 폴더 삭제
    public void deletePostFileStorage(Long postId) throws Exception {
        Path postAttachedFileStorage = getPostFileStoragePath(postId);
        FileUtils.deleteDirectory(postAttachedFileStorage.toFile());
    }

    // 리뷰 데이터 폴더 생성
    public void createReviewFileStorage(Long reviewId) throws Exception {
        Path reviewStorage = getReviewFileStoragePath(reviewId);
        Files.createDirectories(reviewStorage);
    }
    // 리뷰 데이터 폴더 삭제
    public void deleteReviewFileStorage(Long reviewId) throws Exception {
        Path reviewStorage = getReviewFileStoragePath(reviewId);
        FileUtils.deleteDirectory(reviewStorage.toFile());
    }

    // 데이터 파일 삭제
    public void deleteFile(String filePath) {
        FileUtils.fileDelete(filePath);
    }

    // 게시물 데이터 리스트 전체 삭제 (임시)
    public void deletePostFiles(String fileMetadataListJson) {
        Type collectionType = new TypeToken<List<FileMetadata>>(){}.getType();
        List<FileMetadata> fileMetadataList = new Gson()
                .fromJson(fileMetadataListJson, collectionType);

        fileMetadataList.forEach(fileMetadata -> {
            deleteFile(fileMetadata.getUrl());
        });
    }

    // 사용자 프로필 사진 저장
    public String saveAccountPhoto(Long accountId, MultipartFile uploadedFile) throws Exception {
        // 업로드 파일 저장 파일명
        String fileName = "account_profile_photo." + FileUtils.getExtension(Objects.requireNonNull(uploadedFile.getOriginalFilename()));
        // 업로드 파일 저장 경로
        Path savePath = getAccountFileStoragePath(accountId);
        // 업로드 가능한 확장자
        String[] acceptableExtensions = new String[]{
                "jpg","png","jpeg", "gif", "webp"
        };
        // 업로드 파일 용량 제한 (5MB)
        long fileSizeLimit = 5000000;

        // 파일 유효성 검사
        checkFileValidity(savePath, uploadedFile, acceptableExtensions, fileSizeLimit);

        // 파일 저장
        uploadedFile.transferTo(savePath.resolve(fileName));

        return savePath.resolve(fileName).toString();
    }
    
    // 애완동물 프로필 사진 저장
    public String savePetPhoto(Long ownerAccountId, Long petId, MultipartFile uploadedFile) throws Exception {
        // 업로드 파일 저장 파일명
        String fileName = "pet_profile_photo." + FileUtils.getExtension(Objects.requireNonNull(uploadedFile.getOriginalFilename()));
        // 업로드 파일 저장 경로
        Path savePath = getPetFileStoragePath(ownerAccountId, petId);
        // 업로드 가능한 확장자
        String[] acceptableExtensions = new String[]{
                "jpg","png","jpeg", "gif", "webp"
        };
        // 업로드 파일 용량 제한 (5MB)
        long fileSizeLimit = 5000000;

        // 파일 유효성 검사
        checkFileValidity(savePath, uploadedFile, acceptableExtensions, fileSizeLimit);
        
        // 파일 저장
        uploadedFile.transferTo(savePath.resolve(fileName));

        return savePath.resolve(fileName).toString();
    }
    
    // 게시물 미디어파일 저장 전처리
    public List<FileMetadata> savePostMediaAttachments(Long postId, List<MultipartFile> uploadedFiles) throws Exception {
        // 업로드 다중파일 저장 경로
        Path savePath = getPostFileStoragePath(postId).resolve("media");
        // 업로드 가능한 확장자
        String[] acceptableExtensions = new String[]{
                "jpg","png","jpeg", "gif", "webp", "mp4", "webm"
        };
        // 업로드 파일 갯수 제한 및 확인
        int fileCountLimit = 10;
        if (uploadedFiles.size() > fileCountLimit) {
            throw new Exception(msgSrc.getMessage("error.file.count", null, Locale.ENGLISH));
        }

        return this.savePostAttachments(savePath, postId, acceptableExtensions, uploadedFiles, MEDIA_FILE);
    }

    // 게시물 첨부파일 저장 전처리
    public List<FileMetadata> savePostFileAttachments(Long postId, List<MultipartFile> uploadedFiles) throws Exception {
        // 업로드 다중파일 저장 경로
        Path savePath = getPostFileStoragePath(postId).resolve("general");
        // 업로드 가능한 확장자
        String[] acceptableExtensions = new String[]{
                "doc", "docx", "hwp", "pdf", "txt", "ppt", "pptx", "psd", "ai", "xls", "xlsx",
                "rar", "tar", "zip", "exe", "apk"
        };
        // 업로드 파일 갯수 제한 및 확인
        int fileCountLimit = 10;
        if (uploadedFiles.size() > fileCountLimit) {
            throw new Exception(msgSrc.getMessage("error.file.count", null, Locale.ENGLISH));
        }

        return this.savePostAttachments(savePath, postId, acceptableExtensions, uploadedFiles, GENERAL_FILE);
    }

    // 게시물 파일 저장
    private List<FileMetadata> savePostAttachments(
            Path savePath, Long postId, String[] acceptableExtensions, List<MultipartFile> uploadedFiles, Integer fileType
    ) throws Exception {
        // 업로드 개별 파일 용량 제한 (100MB)
        long fileSizeLimit = 100000000;
        // 파일 메타데이터 리스트
        List<FileMetadata> fileMetaDataList = new ArrayList<>();

        // 해당 게시물 데이터 디렉토리 초기화
        FileUtils.cleanDirectory(savePath.toFile());

        for (MultipartFile uploadedFile : uploadedFiles) {
            try {
                // 업로드 파일 저장 파일명 설정
                String fileName = "post_" + postId + "_" + uploadedFile.getOriginalFilename();
                // 파일 유효성 검사
                checkFileValidity(savePath, uploadedFile, acceptableExtensions, fileSizeLimit);
                // 파일 저장
                uploadedFile.transferTo(savePath.resolve(fileName));
                // 파일 메타데이터 정보 생성
                FileMetadata fileMetaData = new FileMetadata(
                        fileName,
                        uploadedFile.getSize(),
                        "post", fileType.equals(MEDIA_FILE) ? "media" : "general",
                        savePath.resolve(fileName).toString()
                );

                fileMetaDataList.add(fileMetaData);
            } catch (Exception e) {
                // 업로드 실패시 해당 게시물 데이터 디렉토리 초기화
                FileUtils.cleanDirectory(savePath.toFile());
                throw e;
            }
        }

        return fileMetaDataList;
    }

    // 리뷰 파일 저장
    public List<FileMetadata> saveReviewAttachments(Long reviewId, List<MultipartFile> uploadedFiles) throws Exception {
        // 업로드 파일 저장 경로
        Path savePath = getReviewFileStoragePath(reviewId);
        // 업로드 가능한 확장자
        String[] acceptableExtensions = new String[]{
                "jpg","png","jpeg", "gif", "webp"
        };
        // 업로드 개별 파일 용량 제한 (10MB)
        long fileSizeLimit = 5000000;
        // 파일 메타데이터 리스트
        List<FileMetadata> fileMetaDataList = new ArrayList<>();

        // 해당 게시물 데이터 디렉토리 초기화
        FileUtils.cleanDirectory(savePath.toFile());

        for (MultipartFile uploadedFile : uploadedFiles) {
            try {
                // 업로드 파일 저장 파일명 설정
                String fileName = "review_" + reviewId + "_" + uploadedFile.getOriginalFilename();
                // 파일 유효성 검사
                checkFileValidity(savePath, uploadedFile, acceptableExtensions, fileSizeLimit);
                // 파일 저장
                uploadedFile.transferTo(savePath.resolve(fileName));
                // 파일 메타데이터 정보 생성
                FileMetadata fileMetaData = new FileMetadata(
                        fileName,
                        uploadedFile.getSize(),
                        "review", "media",
                        savePath.resolve(fileName).toString()
                );

                fileMetaDataList.add(fileMetaData);
            } catch (Exception e) {
                // 업로드 실패시 해당 게시물 데이터 디렉토리 초기화
                FileUtils.cleanDirectory(savePath.toFile());
                throw e;
            }
        }

        return fileMetaDataList;
    }

    // 파일 검증 로직
    public void checkFileValidity(Path savePath, MultipartFile uploadedFile, String[] acceptableExtensions, Long fileSizeLimit) throws Exception {
        // 업로드 파일 원본 파일명
        String originalFileName = uploadedFile.getOriginalFilename();

        // 저장할 데이터 디렉토리 존재 여부 검사
        if (!savePath.toFile().exists()) {
            throw new FileNotFoundException(msgSrc.getMessage("error.dir.notExist", null, Locale.ENGLISH));
        }
        // 빈 파일 검사
        if (uploadedFile.isEmpty()) {
            throw new Exception(msgSrc.getMessage("error.file.empty", new String[]{originalFileName}, Locale.ENGLISH));
        }
        // 파일 확장자 적합성 검사
        else if (Arrays.stream(acceptableExtensions).noneMatch(
                extension -> FileUtils.getExtension(Objects.requireNonNull(originalFileName)).equals(extension)
        )) {
            throw new Exception(msgSrc.getMessage("error.file.extension.valid", new String[]{originalFileName}, Locale.ENGLISH));
        }
        // 파일 크기 적합성 검사
        else if (uploadedFile.getSize() > fileSizeLimit) {
            throw new Exception(msgSrc.getMessage("error.file.size", new String[]{originalFileName}, Locale.ENGLISH));
        }
    }
}
