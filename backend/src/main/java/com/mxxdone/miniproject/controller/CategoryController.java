package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.category.CategoryResponseDto;
import com.mxxdone.miniproject.dto.category.CategorySaveRequestDto;
import com.mxxdone.miniproject.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "카테고리 API", description = "카테고리 관리 API")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "카테고리 생성 (관리자)", description = "새로운 카테고리를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이름 누락, 슬러그 형식 오류 등)"),
    })
    public ResponseEntity<Long> saveCategory(@RequestBody CategorySaveRequestDto requestDto) {
        return ResponseEntity.ok(categoryService.save(requestDto));
    }

    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리를 계층 구조(부모-자식)로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }
}
