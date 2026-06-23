package com.baridmedia.assuranceapi.controller;

import com.baridmedia.assuranceapi.domain.Devis;
import com.baridmedia.assuranceapi.domain.QuoteStatus;
import com.baridmedia.assuranceapi.dto.CreateQuoteRequest;
import com.baridmedia.assuranceapi.dto.QuoteDto;
import com.baridmedia.assuranceapi.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteDto> createQuote(@Valid @RequestBody CreateQuoteRequest req, UriComponentsBuilder uriBuilder) {
        Devis saved = quoteService.createQuote(req.clientId(), req.produitId(), req.montant());
        QuoteDto dto = map(saved);
        URI location = uriBuilder.path("/api/quotes/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping
    public List<QuoteDto> listQuotes(@RequestParam(required = false) QuoteStatus status) {
        return quoteService.listByStatus(status).stream().map(this::map).collect(Collectors.toList());
    }

    @PostMapping("/{id}/approve")
    public QuoteDto approve(@PathVariable Long id) {
        Devis updated = quoteService.approveQuote(id);
        return map(updated);
    }

    private QuoteDto map(Devis d) {
        return new QuoteDto(d.getId(), d.getClient().getId(), d.getProduit().getId(), d.getMontant(), d.getStatut(), d.getDateCreation());
    }
}

