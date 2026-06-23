package com.baridmedia.assuranceapi.controller;

import com.baridmedia.assuranceapi.domain.Produit;
import com.baridmedia.assuranceapi.dto.ProduitDto;
import com.baridmedia.assuranceapi.dto.ProduitRequestDto;
import com.baridmedia.assuranceapi.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProduitDto> createProduct(@Valid @RequestBody ProduitRequestDto req) {
        Produit produit = Produit.builder()
                .code(req.code())
                .libelle(req.libelle())
                .type(req.type())
                .build();

        Produit saved = productService.createProduct(produit);
        ProduitDto dto = new ProduitDto(saved.getId(), saved.getCode(), saved.getLibelle(), saved.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public List<ProduitDto> listProducts() {
        return productService.listAll().stream()
                .map(p -> new ProduitDto(p.getId(), p.getCode(), p.getLibelle(), p.getType()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitDto> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(p -> ResponseEntity.ok(new ProduitDto(p.getId(), p.getCode(), p.getLibelle(), p.getType())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}
