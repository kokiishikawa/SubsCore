package com.subscore.api.repository;

import com.subscore.api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, String> {
    @Query("SELECT c FROM Category c WHERE c.id IN :categoryIds")
    List<Category> findByIdIn(@Param("categoryIds") List<UUID> categoryIds);
}
