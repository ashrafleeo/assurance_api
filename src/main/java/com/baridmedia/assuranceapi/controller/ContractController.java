package com.baridmedia.assuranceapi.controller;

import com.baridmedia.assuranceapi.domain.Contrat;
import com.baridmedia.assuranceapi.dto.ContractDto;
import com.baridmedia.assuranceapi.dto.CreateContractRequest;
import com.baridmedia.assuranceapi.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractDto> createContract(@Valid @RequestBody CreateContractRequest req, UriComponentsBuilder uriBuilder) {
        Contrat saved = contractService.createContract(req.quoteId(), req.dateEffet());
        ContractDto dto = map(saved);
        URI location = uriBuilder.path("/api/contracts/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping
    public List<ContractDto> listContracts() {
        return contractService.listAll().stream().map(this::map).collect(Collectors.toList());
    }

    private ContractDto map(Contrat c) {
        return new ContractDto(c.getId(), c.getDevis().getId(), c.getNumeroContrat(), c.getDateEffet(), c.getStatut());
    }
}

