package com.baridmedia.assuranceapi.dto;

import com.baridmedia.assuranceapi.domain.ContractStatus;
import java.time.LocalDate;

public record ContractDto(Long id, Long quoteId, String numeroContrat, LocalDate dateEffet, ContractStatus statut) {
}

