package com.baridmedia.assuranceapi.service;

import com.baridmedia.assuranceapi.domain.Client;
import com.baridmedia.assuranceapi.exception.ConflictException;
import com.baridmedia.assuranceapi.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService Unit Tests")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .nom("Test Client")
                .email("test@example.com")
                .telephone("+212600000001")
                .createdAt(Instant.now())
                .build();
    }

    // ==================== CREATE CLIENT TESTS ====================

    @Test
    @DisplayName("Should successfully create a new client with valid data")
    void testCreateClient_Success() {
        // Arrange
        when(clientRepository.existsByEmail(testClient.getEmail())).thenReturn(false);
        when(clientRepository.save(testClient)).thenReturn(testClient);

        // Act
        Client result = clientService.createClient(testClient);

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getNom(), result.getNom());
        assertEquals(testClient.getEmail(), result.getEmail());
        verify(clientRepository, times(1)).existsByEmail(testClient.getEmail());
        verify(clientRepository, times(1)).save(testClient);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email is null")
    void testCreateClient_NullEmail_ThrowsIllegalArgumentException() {
        // Arrange
        Client clientWithoutEmail = Client.builder()
                .nom("Test Client")
                .email(null)
                .telephone("+212600000001")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> clientService.createClient(clientWithoutEmail));
        assertEquals("email is required", exception.getMessage());
        verify(clientRepository, never()).existsByEmail(any());
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ConflictException when email already exists")
    void testCreateClient_DuplicateEmail_ThrowsConflictException() {
        // Arrange
        when(clientRepository.existsByEmail(testClient.getEmail())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class,
                () -> clientService.createClient(testClient));
        assertEquals("Email already in use", exception.getMessage());
        verify(clientRepository, times(1)).existsByEmail(testClient.getEmail());
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create client with optional telephone (can be null)")
    void testCreateClient_WithoutTelephone_Success() {
        // Arrange
        Client clientWithoutPhone = Client.builder()
                .nom("Client A")
                .email("clienta@example.com")
                .telephone(null)  // Telephone is optional
                .build();

        when(clientRepository.existsByEmail(clientWithoutPhone.getEmail())).thenReturn(false);
        when(clientRepository.save(clientWithoutPhone)).thenReturn(clientWithoutPhone);

        // Act
        Client result = clientService.createClient(clientWithoutPhone);

        // Assert
        assertNotNull(result);
        assertNull(result.getTelephone());
        verify(clientRepository, times(1)).save(clientWithoutPhone);
    }

    @Test
    @DisplayName("Should create multiple different clients sequentially")
    void testCreateClient_MultipleClients_Success() {
        // Arrange
        Client client1 = Client.builder().nom("Client 1").email("client1@test.com").build();
        Client client2 = Client.builder().nom("Client 2").email("client2@test.com").build();

        when(clientRepository.existsByEmail(client1.getEmail())).thenReturn(false);
        when(clientRepository.existsByEmail(client2.getEmail())).thenReturn(false);
        when(clientRepository.save(client1)).thenReturn(client1);
        when(clientRepository.save(client2)).thenReturn(client2);

        // Act
        Client result1 = clientService.createClient(client1);
        Client result2 = clientService.createClient(client2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(client1.getEmail(), result1.getEmail());
        assertEquals(client2.getEmail(), result2.getEmail());
        verify(clientRepository, times(2)).save(any());
    }

    // ==================== LIST ALL CLIENTS TESTS ====================

    @Test
    @DisplayName("Should retrieve all clients")
    void testListAll_Success() {
        // Arrange
        Client client1 = Client.builder().id(1L).nom("Client 1").email("client1@test.com").build();
        Client client2 = Client.builder().id(2L).nom("Client 2").email("client2@test.com").build();
        Client client3 = Client.builder().id(3L).nom("Client 3").email("client3@test.com").build();
        List<Client> clients = Arrays.asList(client1, client2, client3);

        when(clientRepository.findAll()).thenReturn(clients);

        // Act
        List<Client> result = clientService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(client1.getEmail(), result.get(0).getEmail());
        assertEquals(client2.getEmail(), result.get(1).getEmail());
        assertEquals(client3.getEmail(), result.get(2).getEmail());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no clients exist")
    void testListAll_EmptyList() {
        // Arrange
        when(clientRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Client> result = clientService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(clientRepository, times(1)).findAll();
    }



}

