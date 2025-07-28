package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.CategoryResponseDto;
import com.mxxdone.miniproject.dto.CategorySaveRequestDto;
import com.mxxdone.miniproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Long> saveCategory(@RequestBody CategorySaveRequestDto requestDto) {
        return ResponseEntity.ok(categoryService.save(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }
}
