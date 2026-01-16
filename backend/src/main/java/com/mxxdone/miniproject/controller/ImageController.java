package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.service.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
@Tag(name = "이미지 API", description = "이미지 업로드 API (AWS S3)")
public class ImageController {

    private final S3Uploader s3uploader;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "이미지 파일을 S3에 업로드하고 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공 (이미지 URL 반환)"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 크기 초과"),
            @ApiResponse(responseCode = "500", description = "S3 업로드 실패")
    })
    public ResponseEntity<String> uploadImage(
            @Parameter(
                    description = "업로드할 이미지 파일 (jpg, png 등)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        String imageUrl = s3uploader.upload(image, "posts");
        return ResponseEntity.ok(imageUrl);
    }
}
