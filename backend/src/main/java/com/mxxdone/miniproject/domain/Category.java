package com.mxxdone.miniproject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<Post> posts = new ArrayList<>();

    public Category(String name, String slug, Integer displayOrder) {
        this.name = name;
        this.slug = slug;
        this.displayOrder = displayOrder;
    }

    //양방향 편의 메서드
    public void setParent(Category parent) {
        this.parent = parent;
        if (!parent.getChildren().contains(this)) {
            parent.getChildren().add(this);
        }
    }

    // 자신 + 하위 카테고리 id를 리스트로 반환
    public List<Long> getDescendantIdsAndSelf() {
        List<Long> ids = new ArrayList<>();
        collectIds(this, ids);
        return ids;
    }

    private void collectIds(Category category, List<Long> ids) {
        ids.add(category.getId());
        for (Category child : category.getChildren()) {
            collectIds(child, ids);
        }
    }
}
