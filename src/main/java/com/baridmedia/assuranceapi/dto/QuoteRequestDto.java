package com.baridmedia.assuranceapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuoteRequestDto(
        @NotNull Long clientId,
        @NotNull Long produitId,
        @NotNull @Min(0) Double montant
) {}

