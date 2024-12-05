package com.crudoperations.crudOperations.repository;

import com.crudoperations.crudOperations.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
