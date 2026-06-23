package com.baridmedia.assuranceapi.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateContractRequest(
        @NotNull Long quoteId,
        @NotNull LocalDate dateEffet
) {}

