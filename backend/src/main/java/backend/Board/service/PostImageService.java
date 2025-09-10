package backend.Board.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostImageService {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    private static final int MAX_IMAGE_COUNT = 3;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 게시글 이미지 업로드 (최대 3장)
     */
    public List<String> uploadPostImages(List<MultipartFile> images) {
        validateImages(images);

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            try {
                String imageUrl = uploadSingleImage(image);
                imageUrls.add(imageUrl);
            } catch (Exception e) {
                // 이미 업로드된 이미지들 롤백
                rollbackUploadedImages(imageUrls);
                throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }

        return imageUrls;
    }

    /**
     * 단일 이미지 업로드
     */
    public String uploadSingleImage(MultipartFile image) {
        validateSingleImage(image);

        String key = generateImageKey(image.getOriginalFilename());
        ObjectMetadata metadata = createMetadata(image);

        uploadToS3(image, key, metadata);

        return getImageUrl(key);
    }

    /**
     * 이미지 유효성 검사
     */
    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        }

        if (images.size() > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 업로드 가능합니다.");
        }

        for (MultipartFile image : images) {
            validateSingleImage(image);
        }
    }

    /**
     * 단일 이미지 유효성 검사
     */
    private void validateSingleImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        if (image.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("이미지 파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 지원하는 이미지 형식 확인
        if (!isValidImageFormat(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (지원 형식: JPEG, PNG, GIF, WebP)");
        }
    }

    /**
     * 지원하는 이미지 형식 확인
     */
    private boolean isValidImageFormat(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp");
    }

    /**
     * 이미지 키 생성
     */
    private String generateImageKey(String originalFilename) {
        String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        return "post-images/" + savedFilename;
    }

    /**
     * S3 메타데이터 생성
     */
    private ObjectMetadata createMetadata(MultipartFile image) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(image.getSize());
        metadata.setContentType(image.getContentType());
        return metadata;
    }

    /**
     * S3에 이미지 업로드
     */
    private void uploadToS3(MultipartFile image, String key, ObjectMetadata metadata) {
        try {
            amazonS3.putObject(bucketName, key, image.getInputStream(), metadata);
            log.info("이미지 업로드 성공: {}", key);
        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", key, e);
            throw new RuntimeException("S3 이미지 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 이미지 URL 생성
     */
    private String getImageUrl(String key) {
        URL url = amazonS3.getUrl(bucketName, key);
        if (url == null) {
            throw new RuntimeException("S3에서 이미지 URL을 가져올 수 없습니다.");
        }
        return url.toString();
    }

    /**
     * 이미지 삭제
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        String key = extractKeyFromUrl(imageUrl);
        log.info("삭제 시도할 S3 key: {}", key);

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
            log.info("이미지 삭제 완료: {}", key);
        } catch (AmazonServiceException e) {
            log.error("AmazonServiceException 발생: {}", e.getErrorMessage(), e);
            throw new RuntimeException("S3 삭제 실패: " + e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("SdkClientException 발생: {}", e.getMessage(), e);
            throw new RuntimeException("S3 클라이언트 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 여러 이미지 삭제
     */
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (String imageUrl : imageUrls) {
            try {
                deleteImage(imageUrl);
            } catch (Exception e) {
                log.error("이미지 삭제 실패: {}", imageUrl, e);
                // 다른 이미지 삭제를 계속 진행
            }
        }
    }

    /**
     * URL에서 S3 키 추출
     */
    private String extractKeyFromUrl(String imageUrl) {
        // URL 형식에 따라 키 추출
        String httpsPrefix = "https://" + bucketName + ".s3.amazonaws.com/";
        String s3Prefix = "s3://" + bucketName + "/";

        if (imageUrl.startsWith(httpsPrefix)) {
            return imageUrl.replace(httpsPrefix, "");
        } else if (imageUrl.startsWith(s3Prefix)) {
            return imageUrl.replace(s3Prefix, "");
        }

        // 직접 키가 전달된 경우
        return imageUrl;
    }

    /**
     * 업로드 실패 시 이미 업로드된 이미지들 롤백
     */
    private void rollbackUploadedImages(List<String> uploadedImageUrls) {
        if (uploadedImageUrls == null || uploadedImageUrls.isEmpty()) {
            return;
        }

        log.warn("이미지 업로드 실패로 인한 롤백 시작. 삭제할 이미지 수: {}", uploadedImageUrls.size());

        for (String imageUrl : uploadedImageUrls) {
            try {
                deleteImage(imageUrl);
                log.info("롤백 삭제 완료: {}", imageUrl);
            } catch (Exception e) {
                log.error("롤백 삭제 실패: {}", imageUrl, e);
            }
        }
    }
}