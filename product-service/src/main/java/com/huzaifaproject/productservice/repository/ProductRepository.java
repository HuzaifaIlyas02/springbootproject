package com.huzaifaproject.productservice.repository;

import com.huzaifaproject.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
