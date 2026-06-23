package com.baridmedia.assuranceapi.controller;

import com.baridmedia.assuranceapi.domain.Client;
import com.baridmedia.assuranceapi.dto.ClientDto;
import com.baridmedia.assuranceapi.dto.ClientRequestDto;
import com.baridmedia.assuranceapi.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientRequestDto req) {
        Client client = Client.builder()
                .nom(req.nom())
                .email(req.email())
                .telephone(req.telephone())
                .build();

        Client saved = clientService.createClient(client);
        ClientDto dto = new ClientDto(saved.getId(), saved.getNom(), saved.getEmail(), saved.getTelephone());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public List<ClientDto> listClients() {
        return clientService.listAll().stream()
                .map(c -> new ClientDto(c.getId(), c.getNom(), c.getEmail(), c.getTelephone()))
                .toList();
    }
}

