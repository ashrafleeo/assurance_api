package com.baridmedia.assuranceapi.controller;

import com.baridmedia.assuranceapi.domain.Devis;
import com.baridmedia.assuranceapi.domain.QuoteStatus;
import com.baridmedia.assuranceapi.dto.QuoteRequestDto;
import com.baridmedia.assuranceapi.dto.QuoteDto;
import com.baridmedia.assuranceapi.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteDto> createQuote(@Valid @RequestBody QuoteRequestDto req) {
        Devis saved = quoteService.createQuote(req.clientId(), req.produitId(), req.montant());
        QuoteDto quoteDto = map(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteDto);
    }

    @GetMapping
    public ResponseEntity<List<QuoteDto>> listQuotes(@RequestParam(required = false) QuoteStatus status) {
        List<Devis> devisList = quoteService.listByStatus(status);
        List<QuoteDto> result = devisList.stream()
                .map(this::map)
                .toList();
        return ResponseEntity.ok(result);
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

