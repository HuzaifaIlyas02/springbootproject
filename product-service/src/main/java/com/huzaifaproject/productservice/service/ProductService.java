package com.huzaifaproject.productservice.service;

import com.huzaifaproject.productservice.dto.DecreaseQuantityRequest;
import com.huzaifaproject.productservice.dto.ProductRequest;
import com.huzaifaproject.productservice.dto.ProductResponse;
import com.huzaifaproject.productservice.exception.ProductNotFoundException;
import com.huzaifaproject.productservice.model.Product;
import com.huzaifaproject.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();

        productRepository.save(product);
        log.info("Product {} is saved", product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(this::mapToProductResponse).toList();
    }

    public ProductResponse getProductById(String id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        log.info("Product {} is retrieved", product.getId());
        return mapToProductResponse(product);
    }

    public void updateProduct(String id, ProductRequest productRequest) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());

        productRepository.save(product);
        log.info("Product {} is updated", product.getId());
    }

    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product {} is deleted", id);
    }
    
    public void decreaseQuantity(DecreaseQuantityRequest request) {
        List<Product> products = productRepository.findAll();

        Product product = products.stream()
                .filter(p -> p.getName().toLowerCase().replace(" ", "_").equals(request.getSkuCode()))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found with skuCode: " + request.getSkuCode()));

    Integer currentQuantity = product.getQuantity();
    int requestedQuantity = request.getQuantity() == null ? 0 : request.getQuantity();
    int updatedQuantity = Math.max(0, (currentQuantity == null ? 0 : currentQuantity) - requestedQuantity);
        product.setQuantity(updatedQuantity);
        productRepository.save(product);
        log.info("Decreased quantity for product {} by {}. New quantity: {}",
        product.getName(), requestedQuantity, product.getQuantity());
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }
}
