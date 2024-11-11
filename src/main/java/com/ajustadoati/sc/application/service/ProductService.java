
package com.ajustadoati.sc.application.service;


import com.ajustadoati.sc.adapter.rest.repository.ProductRepository;
import com.ajustadoati.sc.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

  @Autowired
  private ProductRepository productRepository;

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Optional<Product> getProductById(Integer id) {
    return productRepository.findById(id);
  }

  public Product createProduct(Product product) {
    return productRepository.save(product);
  }

  public Product updateProduct(Integer id, Product updatedProduct) {
    return productRepository.findById(id).map(product -> {
      product.setName(updatedProduct.getName());
      return productRepository.save(product);
    }).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
  }

  public void deleteProduct(Integer id) {
    productRepository.deleteById(id);
  }

}
