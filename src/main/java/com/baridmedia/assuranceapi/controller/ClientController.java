package com.baridmedia.assuranceapi.controller;

import com.baridmedia.assuranceapi.domain.Client;
import com.baridmedia.assuranceapi.dto.ClientDto;
import com.baridmedia.assuranceapi.dto.CreateClientRequest;
import com.baridmedia.assuranceapi.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody CreateClientRequest req, UriComponentsBuilder uriBuilder) {
        Client client = Client.builder()
                .nom(req.nom())
                .email(req.email())
                .telephone(req.telephone())
                .build();

        Client saved = clientService.createClient(client);
        ClientDto dto = new ClientDto(saved.getId(), saved.getNom(), saved.getEmail(), saved.getTelephone());
        URI location = uriBuilder.path("/api/clients/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping
    public List<ClientDto> listClients() {
        return clientService.listAll().stream()
                .map(c -> new ClientDto(c.getId(), c.getNom(), c.getEmail(), c.getTelephone()))
                .collect(Collectors.toList());
    }
}

