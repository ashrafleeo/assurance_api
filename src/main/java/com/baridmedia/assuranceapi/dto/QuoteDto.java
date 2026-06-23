package com.baridmedia.assuranceapi.dto;

import com.baridmedia.assuranceapi.domain.QuoteStatus;
import java.time.Instant;

public record QuoteDto(Long id, Long clientId, Long produitId, Double montant, QuoteStatus statut, Instant dateCreation) {
}

