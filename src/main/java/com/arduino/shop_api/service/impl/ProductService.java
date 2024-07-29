package com.arduino.shop_api.service.impl;

import com.arduino.shop_api.entity.Category;
import com.arduino.shop_api.entity.Product;
import com.arduino.shop_api.exception.NotFoundException;
import com.arduino.shop_api.model.request.ProductRequest;
import com.arduino.shop_api.model.response.GeneralResponse;
import com.arduino.shop_api.model.response.MetadataResponse;
import com.arduino.shop_api.repository.CategoryRepository;
import com.arduino.shop_api.repository.ProductRepository;
import com.arduino.shop_api.service.contract.IProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.arduino.shop_api.model.response.ResponseGenerator.buildResponse;

@Service
public class ProductService implements IProductService {
    private ProductRepository repository;

    private CategoryRepository categoryRepository;

    public ProductService(ProductRepository repository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public GeneralResponse<MetadataResponse, List<Product>> get(Integer categoryId) {
        // Retrieve data from database
        // Search by category if categoryId is present
        Category category = null != categoryId ? this.categoryRepository.findById((long) categoryId).orElseThrow(()->new NotFoundException("Category Not Found")) : null;

        List<Product> products = category != null ? this.repository.findByCategory(category) : this.repository.findAll();

        return buildResponse(products, HttpStatus.OK, null);
    }

    @Override
    public GeneralResponse<MetadataResponse, Product> get(int id) {
        // Get data from database by ID
        // Throw exception if data is not found
        Optional<Product> product =  this.repository.findById((long) id);

        if (!product.isPresent()) {
            throw new NotFoundException("Product not found");
        }

        return buildResponse(product.get(), HttpStatus.OK, "Product Found");
    }

    @Override
    public GeneralResponse<MetadataResponse, Boolean> store(ProductRequest request) {
        // Get category by ID from request
        Optional<Category> category = this.categoryRepository.findById(request.getCategoryId());

        if (!category.isPresent()) {
            throw new NotFoundException("Category not found");
        }

        // Persist data to DB
        persist(request, null, category.get());

        return buildResponse(true, HttpStatus.CREATED, null);
    }

    @Override
    public GeneralResponse<MetadataResponse, Boolean> update(ProductRequest request, int id) {
        // Search Product by ID
        Product product = this.repository.findById((long) id).orElseThrow(() -> new NotFoundException("Product not found"));

        // Search category by ID
        Category category = this.categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        persist(request,product,category);

        return buildResponse(true, HttpStatus.OK, "Product Updated");
    }

    @Override
    public GeneralResponse<MetadataResponse, Boolean> delete(int id) {
        // Search by ID
        // Delete
        Product product = this.repository.findById((long) id).orElseThrow(() -> new NotFoundException("Product not found"));

        this.repository.delete(product);

        return buildResponse(true, HttpStatus.OK, "Product Deleted");
    }

    private void persist(ProductRequest request, Product product, Category category) {
        // Create new if product is not exists
        if (null == product) {
            product = new Product();
        }

        // Map request to entity
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUnit(request.getUnit());
        product.setCategory(category);
        product.setSpecification(request.getSpecification());
        product.setLocation(request.getLocation());
        product.setUpdatedAt(OffsetDateTime.now());

        this.repository.saveAndFlush(product);
    }
}