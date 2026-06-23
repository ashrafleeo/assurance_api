package com.baridmedia.assuranceapi.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ContractRequestDto(
        @NotNull Long quoteId,
        @NotNull LocalDate dateEffet
) {}

