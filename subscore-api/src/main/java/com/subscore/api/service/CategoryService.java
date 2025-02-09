package com.subscore.api.service;


import com.subscore.api.model.Category;
import com.subscore.api.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getCategorAll() {
        return categoryRepository.findAll();
    }
}
