package com.baridmedia.assuranceapi.service;

import com.baridmedia.assuranceapi.domain.Produit;
import com.baridmedia.assuranceapi.exception.ConflictException;
import com.baridmedia.assuranceapi.exception.BusinessException;
import com.baridmedia.assuranceapi.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProduitRepository produitRepository;

    @Transactional
    public Produit createProduct(Produit product) {
        if (product.getCode() == null) throw new IllegalArgumentException("code is required");
        if (produitRepository.existsByCode(product.getCode())) {
            throw new ConflictException("Product code already in use");
        }
        return produitRepository.save(product);
    }

    public List<Produit> listAll() {
        return produitRepository.findAll();
    }

    public Optional<Produit> findById(Long id) {
        return produitRepository.findById(id);
    }


    public void deleteProduct(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new BusinessException("Product not found");
        }
        produitRepository.deleteById(id);
    }

}
