package com.baridmedia.assuranceapi.service;

import com.baridmedia.assuranceapi.domain.Produit;
import com.baridmedia.assuranceapi.exception.ResourceNotFoundException;
import com.baridmedia.assuranceapi.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;

    public Produit findById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit not found: " + id));
    }
}

