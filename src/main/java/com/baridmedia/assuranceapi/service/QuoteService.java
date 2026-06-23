package com.baridmedia.assuranceapi.service;

import com.baridmedia.assuranceapi.domain.Client;
import com.baridmedia.assuranceapi.domain.Devis;
import com.baridmedia.assuranceapi.domain.QuoteStatus;
import com.baridmedia.assuranceapi.domain.Produit;
import com.baridmedia.assuranceapi.exception.BusinessException;
import com.baridmedia.assuranceapi.exception.ResourceNotFoundException;
import com.baridmedia.assuranceapi.repository.DevisRepository;
import com.baridmedia.assuranceapi.repository.ClientRepository;
import com.baridmedia.assuranceapi.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final DevisRepository devisRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;

    private static final double MANAGER_THRESHOLD = 10000.0;

    @Transactional
    public Devis createQuote(Long clientId, Long produitId, Double montant) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit not found: " + produitId));

        if (montant == null || montant < 0) {
            throw new BusinessException("Montant invalide");
        }

        Devis devis = Devis.builder()
                .client(client)
                .produit(produit)
                .montant(montant)
                .build();

        if (montant <= MANAGER_THRESHOLD) {
            devis.setStatut(QuoteStatus.APPROVED);
        } else {
            devis.setStatut(QuoteStatus.PENDING_MANAGER);
        }

        return devisRepository.save(devis);
    }

    public List<Devis> listByStatus(QuoteStatus statut) {
        if (statut == null) return devisRepository.findAll();
        return devisRepository.findByStatut(statut);
    }

    @Transactional
    public Devis approveQuote(Long quoteId) {
        Devis devis = devisRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Devis not found: " + quoteId));
        if (devis.getStatut() != QuoteStatus.PENDING_MANAGER) {
            throw new BusinessException("Devis not in PENDING_MANAGER state");
        }
        devis.setStatut(QuoteStatus.APPROVED);
        return devisRepository.save(devis);
    }
}

