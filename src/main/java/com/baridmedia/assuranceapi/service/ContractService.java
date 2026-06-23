package com.baridmedia.assuranceapi.service;

import com.baridmedia.assuranceapi.domain.Contrat;
import com.baridmedia.assuranceapi.domain.ContractStatus;
import com.baridmedia.assuranceapi.domain.Devis;
import com.baridmedia.assuranceapi.domain.QuoteStatus;
import com.baridmedia.assuranceapi.exception.BusinessException;
import com.baridmedia.assuranceapi.exception.ConflictException;
import com.baridmedia.assuranceapi.exception.ResourceNotFoundException;
import com.baridmedia.assuranceapi.repository.ContratRepository;
import com.baridmedia.assuranceapi.repository.DevisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContratRepository contratRepository;
    private final DevisRepository devisRepository;

    @Transactional
    public Contrat createContract(Long quoteId, LocalDate dateEffet) {
        Devis devis = devisRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Devis not found: " + quoteId));

        if (devis.getStatut() != QuoteStatus.APPROVED) {
            throw new BusinessException("Cannot create contract from non-approved quote");
        }

        if (contratRepository.existsByDevis(devis)) {
            throw new ConflictException("Contract already exists for this quote");
        }

        String numero = generateNumeroContrat();
        Contrat contrat = Contrat.builder()
                .devis(devis)
                .numeroContrat(numero)
                .dateEffet(dateEffet)
                .statut(ContractStatus.ACTIVE)
                .build();

        return contratRepository.save(contrat);
    }

    public List<Contrat> listAll() {
        return contratRepository.findAll();
    }

    private String generateNumeroContrat() {
        return "CT-" + UUID.randomUUID();
    }
}

