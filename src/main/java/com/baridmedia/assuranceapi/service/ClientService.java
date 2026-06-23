package com.baridmedia.assuranceapi.service;

import com.baridmedia.assuranceapi.domain.Client;
import com.baridmedia.assuranceapi.exception.ConflictException;
import com.baridmedia.assuranceapi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public Client createClient(Client client) {
        if (client.getEmail() == null) throw new IllegalArgumentException("email is required");
        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new ConflictException("Email already in use");
        }
        return clientRepository.save(client);
    }

    public List<Client> listAll() {
        return clientRepository.findAll();
    }

}

