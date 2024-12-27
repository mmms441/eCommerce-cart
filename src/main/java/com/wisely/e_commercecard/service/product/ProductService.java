package com.wisely.e_commercecard.service.product;

import com.wisely.e_commercecard.exception.ResourceNotFoundException;
import com.wisely.e_commercecard.model.Category;
import com.wisely.e_commercecard.model.Product;
import com.wisely.e_commercecard.repository.CategoryRepository;
import com.wisely.e_commercecard.repository.ProductRepository;
import com.wisely.e_commercecard.requsets.AddProductRequest;
import com.wisely.e_commercecard.requsets.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    @Override
    public Product addProduct(AddProductRequest request){
        Category category = Optional.ofNullable(categoryRepository
                        .findByName(request.getCategory().getName()))
                .orElseGet(()->{
                    List<Product> categories = new ArrayList<>();
                    Category newCategory = new Category(1L,request.getCategory().getName() ,categories);
                    return categoryRepository.save(newCategory);
                });
        request.setCategory(category);
        return productRepository.save(createproduct(request,category));
    }
   /* public Product addProduct(AddProductRequest request) {
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(()->{
                    Category newCategory = new Category(request.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        return null;
    }*/
    public Product createproduct(AddProductRequest request , Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category

        );

    }
    @Override
    public Product getProductById(Long id) {

        return productRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("product not found"));
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.findById(id).ifPresentOrElse(productRepository::delete,
                ()-> {throw new ResourceNotFoundException("product not found");});

    }

    @Override
    public Product updateProduct( ProductUpdateRequest request, Long productId) {
        return productRepository.findById(productId)
                .map(existingProduct->updateExistingProduct(existingProduct , request))
                .map(productRepository::save)
                .orElseThrow(()->new ResourceNotFoundException("product not found"));
    }

    public Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription() );

        Category category = categoryRepository.findByName(request.getCategory().getName());
        existingProduct.setCategory(category);
        return existingProduct;
    }
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category,brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand,name);
    }

    @Override
    public Long countProductsBtBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand,name);
    }
}
