package com.baridmedia.assuranceapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateClientRequest(
        @NotBlank String nom,
        @NotBlank @Email String email,
        String telephone
) {}

