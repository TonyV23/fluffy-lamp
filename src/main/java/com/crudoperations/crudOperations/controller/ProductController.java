package com.crudoperations.crudOperations.controller;


import com.crudoperations.crudOperations.dto.ProductDto;
import com.crudoperations.crudOperations.model.Product;
import com.crudoperations.crudOperations.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/create";
    }

    @PostMapping("/create")
    public String processCreateForm(@Valid @ModelAttribute("productDto") ProductDto productDto, BindingResult result) {
        if (productDto.getImageFilePath().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFilePath", "Image file is required"));
        }
        if (result.hasErrors()) {
            return "products/create";
        }

        // save image file in database
        MultipartFile file = productDto.getImageFilePath();
        Date createDate = new Date();
        String storageFileName = createDate.getTime() + "_" + file.getOriginalFilename();

        try {
            String uploadDirectory = System.getProperty("public/images");
            Path path = Paths.get(uploadDirectory, storageFileName);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            try(InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            }

        } catch (Exception e) {
            System.err.println("This error occurs :"+e.getMessage());
        }

        // saving the product in database
        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(productDto.getCategory());
        product.setCreatedAt(createDate);
        product.setImageFilePath(storageFileName);

        productRepository.save(product);

        return "redirect:/products";
    }

}
