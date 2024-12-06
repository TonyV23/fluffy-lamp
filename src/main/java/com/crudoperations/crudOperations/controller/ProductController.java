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
import org.springframework.web.bind.annotation.*;
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


    @GetMapping("/edit")
    public String showEditForm(Model model, @RequestParam int id) {
        try {
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setDescription(product.getDescription());
            productDto.setPrice(product.getPrice());
            productDto.setCategory(product.getCategory());
            model.addAttribute("productDto", productDto);

        } catch (Exception e) {
            System.err.println("This error occurs :"+e.getMessage());
            return "redirect:/products";
        }

        return "products/edit";
    }

    @PostMapping("/edit")
    public String updateProduct(@Valid @ModelAttribute("productDto") @RequestParam int id, ProductDto productDto, BindingResult result, Model model) {
       try {
           Product product = productRepository.findById(id).get();
           model.addAttribute("product", product);
           if (result.hasErrors()) {
               return "products/edit";
           }
           if (!productDto.getImageFilePath().isEmpty()) {
               //deleting old image
               String uploadDirectory = System.getProperty("public/images");
               Path oldImagePath = Paths.get(uploadDirectory+productDto.getImageFilePath());

               try {
                   Files.delete(oldImagePath);
               } catch (Exception e) {
                   System.err.println("This error occurs :"+e.getMessage());
               }

               // saving the new image
               MultipartFile file = productDto.getImageFilePath();
               Date updateDate = new Date();
               String storageFileName = updateDate.getTime()+"_" + file.getOriginalFilename();

               try (InputStream inputStream = file.getInputStream()) {
                   Files.copy(inputStream, Paths.get(uploadDirectory+"_"+storageFileName), StandardCopyOption.REPLACE_EXISTING);

               } catch (Exception e) {
                   System.err.println("This error occurs :"+e.getMessage());
               }
               product.setImageFilePath(storageFileName);
           }

           product.setName(productDto.getName());
           product.setBrand(productDto.getBrand());
           product.setDescription(productDto.getDescription());
           product.setPrice(productDto.getPrice());
           product.setCategory(productDto.getCategory());

           productRepository.save(product);
       } catch (Exception e) {
           System.err.println("This error occurs :"+e.getMessage());
       }


        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Product product = productRepository.findById(id).get();
            // before deleting product in database , we must before delete image in public/
            Path imagePath = Paths.get("public/images/" +product.getImageFilePath());
            try {
                Files.delete(imagePath);
            }catch (Exception e) {
                System.err.println("This error occurs :"+e.getMessage());
            }

            //delete the product
            productRepository.delete(product);
        } catch (Exception e) {
            System.err.println("This error occurs :"+e.getMessage());
        }
        return "redirect:/products";
    }
}
