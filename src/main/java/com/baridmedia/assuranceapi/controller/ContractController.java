package com.baridmedia.assuranceapi.controller;

import com.baridmedia.assuranceapi.domain.Contrat;
import com.baridmedia.assuranceapi.dto.ContractDto;
import com.baridmedia.assuranceapi.dto.ContractRequestDto;
import com.baridmedia.assuranceapi.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractDto> createContract(@Valid @RequestBody ContractRequestDto req) {
        Contrat saved = contractService.createContract(req.quoteId(), req.dateEffet());
        ContractDto cdto = ContractToDto(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(cdto);
    }

    @GetMapping
    public List<ContractDto> listContracts() {
        return contractService.listAll().stream().map(this::ContractToDto).toList();
    }

    private ContractDto ContractToDto(Contrat c) {
        return new ContractDto(c.getId(), c.getDevis().getId(), c.getNumeroContrat(), c.getDateEffet(), c.getStatut());
    }
}

