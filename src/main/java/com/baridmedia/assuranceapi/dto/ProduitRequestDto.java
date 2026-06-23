package com.baridmedia.assuranceapi.dto;

import jakarta.validation.constraints.NotBlank;

public record ProduitRequestDto(
        @NotBlank String code,
        @NotBlank String libelle,
        String type
) {}

