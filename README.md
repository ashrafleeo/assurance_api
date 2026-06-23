# AssuranceApi — API REST de Gestion d'Assurance

Une API REST Spring Boot moderne pour gérer les devis d'assurance, les contrats, les clients et les produits. Conçue pour les workflows métier réels du secteur assurantiel.


## 🎯 Vue d'ensemble

**AssuranceApi** est un service backend implémentant le cycle complet d'une demande d'assurance :
1. Enregistrement des clients et produits
2. Création de devis (approbation automatique pour les petits montants, manuelle pour les gros)
3. Gestion des workflows d'approbation
4. Génération et activation des contrats d'assurance

### Stack technologique

- **Framework** : Spring Boot 3.x
- **Langage** : Java 17+
- **Base de données** : H2 en mémoire (développement) ; configurable pour production
- **Build** : Maven
- **API** : REST/JSON
- **Validation** : Jakarta Bean Validation
- **ORM** : JPA/Hibernate
- **Lombok** : Auto-génération des getters, setters, builders

---

## 📊 Modèle de données

### Vue d'ensemble entités

```
┌─────────────┐     ┌──────────────┐
│   Client    │     │   Produit    │
└──────┬──────┘     └───────┬──────┘
       │                    │
       │ 1         N        │ 1
       └────────┬───────────┘
                │
             ┌──▼───┐
             │ Devis│ (Quote)
             └──┬───┘
                │ 1
                │ N
          ──────▼────┐
         │  Contrat  │
         └───────────┘
```

### 1. Client (Entité)

**Responsabilité** : Représenter un assuré/demandeur

```sql
CREATE TABLE client (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

| Champ | Type | Contrainte | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant unique |
| nom | String | NOT NULL | Nom complet du client |
| email | String | NOT NULL, UNIQUE | Email unique (identifiant métier) |
| telephone | String | Optionnel | N° de téléphone contact |
| dateCreation | LocalDateTime | AUTO | Timestamp création |

**Règles métier** :
- Email est obligatoire et unique (409 Conflict si doublon)
- Le client peut avoir plusieurs devis
- Le client ne peut être supprimé s'il a des devis/contrats liés

---

### 2. Produit (Entité)

**Responsabilité** : Représenter une offre d'assurance (Auto, Habitation, Voyage, etc.)

```sql
CREATE TABLE produit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

| Champ | Type | Contrainte | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant unique |
| code | String | NOT NULL, UNIQUE | Code produit unique (ex: AUTO-001) |
| libelle | String | NOT NULL | Description (ex: Assurance Auto Premium) |
| type | String | Optionnel | Catégorie (AUTO, HOME, TRAVEL) |
| dateCreation | LocalDateTime | AUTO | Timestamp création |

**Règles métier** :
- Code est obligatoire et unique (409 Conflict si doublon)
- Un produit peut être utilisé dans plusieurs devis
- Pas de suppression si utilisé dans des devis actifs

---

### 3. Devis / Quote (Entité)

**Responsabilité** : Représenter une proposition d'assurance (offre tarifée) pour un client

```sql
CREATE TABLE devis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    montant DECIMAL(10, 2) NOT NULL,
    statut VARCHAR(50) DEFAULT 'DRAFT',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES client(id),
    FOREIGN KEY (produit_id) REFERENCES produit(id)
);
```

| Champ | Type | Contrainte | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant unique |
| clientId | BIGINT | FK | Références Client |
| produitId | BIGINT | FK | Références Produit |
| montant | Double | NOT NULL, ≥ 0 | Montant d'assurance proposé |
| statut | Enum | DEFAULT DRAFT | État du devis |
| dateCreation | LocalDateTime | AUTO | Timestamp création |

**Statuts du devis (État machine)** :

```
DRAFT → PENDING_MANAGER → APPROVED → [Contract créé] ou REJECTED
        ↓
    [si montant ≤ 10k → auto APPROVED]
    [si montant > 10k → PENDING_MANAGER (approb manuelle)]
```

| Statut | Condition | Action possible | Montant |
|--------|-----------|-----------------|---------|
| DRAFT | Brouillon initial | Créer un contrat ? ❌ | — |
| PENDING_MANAGER | En revue manager | Approuver ou rejeter | > 10 000 |
| APPROVED | Approuvé | ✅ Créer contrat | Tous |
| REJECTED | Rejeté | Aucune action | — |

**Règles métier (critique)** :
- **Auto-approbation** : Si montant ≤ 10 000 → statut APPROVED immédiatement
- **Approbation manuelle** : Si montant > 10 000 → statut PENDING_MANAGER (nécessite appel `/approve`)
- Client et Produit doivent exister (404 sinon)
- Montant doit être ≥ 0 (422 sinon)

---

### 4. Contrat (Entité)

**Responsabilité** : Représenter un contrat d'assurance actif/signé

```sql
CREATE TABLE contrat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    devis_id BIGINT NOT NULL UNIQUE,
    numero_contrat VARCHAR(100) NOT NULL UNIQUE,
    date_effet DATE NOT NULL,
    statut VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (devis_id) REFERENCES devis(id)
);
```

| Champ | Type | Contrainte | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PK, AUTO | Identifiant unique |
| devisId | BIGINT | FK, UNIQUE | Référence Devis (1-to-1) |
| numeroContrat | String | UNIQUE | N° généré unique (CT-UUID) |
| dateEffet | LocalDate | NOT NULL | Date de début de couverture |
| statut | Enum | DEFAULT ACTIVE | État du contrat |
| dateCreation | LocalDateTime | AUTO | Timestamp création |

**Statuts du contrat** :

| Statut | Description |
|--------|-------------|
| ACTIVE | Contrat en vigueur |
| SUSPENDED | Suspendu (paiement non reçu) |
| EXPIRED | Expiré (fin de période) |
| CANCELLED | Annulé |

**Règles métier** :
- Le devis source doit être en statut APPROVED (422 sinon)
- Un seul contrat par devis (409 Conflict si existe)
- Une fois créé, le contrat ne peut être créé à nouveau pour le même devis
- Le numéro de contrat est auto-généré (format: CT-{UUID})

---

## 🏗️ Architecture applicative

### Vue en couches

```
┌───────────────────────────────────────────────┐
│           REST Controller Layer                │
│  (ProductController, ClientController, etc.)  │
│  ↓ Responsabilité: Recevoir/valider requêtes  │
├───────────────────────────────────────────────┤
│     DTO / Mapping Layer (Convertisseurs)      │
│  (ProduitDto, ClientDto, etc.)               │
│  ↓ Responsabilité: Mapper Entity ↔ DTO       │
├───────────────────────────────────────────────┤
│          Service Layer (Métier)                │
│  (ProductService, ClientService, etc.)        │
│  ↓ Responsabilité: Logique métier, transitions│
├───────────────────────────────────────────────┤
│        Repository Layer (Persistence)         │
│  (ProduitRepository, ClientRepository, etc.)  │
│  ↓ Responsabilité: Accès BD via JPA          │
├───────────────────────────────────────────────┤
│         Database Layer (H2 ou PostgreSQL)     │
│  ↓ Responsabilité: Persistance données        │
└───────────────────────────────────────────────┘
```

## 🔍 Justifications des choix techniques

### 1️⃣ **Pourquoi les DTO (Data Transfer Objects) ?**

| Avantage | Détail |
|----------|--------|
| **Sécurité** | Ne pas exposer tous les champs Entity (ex: pwd, timestamps internes) |
| **Flexibilité API** | Changer structure Entity sans casser clients API |
| **Validation au point d'entrée** | Annotations `@NotNull`, `@Email` dans DTO = validation au boundary |
| **Sérialisation contrôlée** | Inclure/exclure champs selon contexte (ex: gros payload → allégé) |
| **Contrats clairs** | DTO = contrat d'API documenté, stable |

---

### 2️⃣ **Pourquoi une couche Service (métier) ?**

| Raison | Détail |
|--------|--------|
| **DRY (Don't Repeat Yourself)** | Logique métier centralisée → réutilisable (API, batch, scheduler) |
| **Transactionnalité** | `@Transactional` multi-requêtes (ex: devis → contrat atomique) |
| **Testabilité** | Tester Service sans HTTP, sans BD (mock Repositories) |
| **Séparation des concerns** | Service = métier, Controller = HTTP, Repository = SQL |
| **Règles métier cohérentes** | Même règle appliquée partout (ex: seuil 10k pour approbation) |

**Exemple** :
```java
// ✅ Service = logique métier réutilisable
@Service @Transactional
public class QuoteService {
    public Devis createQuote(Long clientId, Long produitId, Double montant) {
        // Logique complexe : validation, auto-approbation, etc.
    }
}

// Utilisable de plusieurs contextes
// 1. API Controller → HTTP
// 2. Batch scheduler → créer devis auto la nuit
// 3. CLI tool → import CSV de devis
// 4. Unit tests → sans HTTP
```

---

### 3️⃣ **Pourquoi la validation par annotations (Jakarta Validation) ?**

| Approche | Pros | Cons |
|----------|------|------|
| **Annotations DTO** | Déclaratif, centralisé, automatique Spring, clair | Limité à champs simples |
| **Logique Service** | Logique complexe, règles métier | Code verbeux, facile oublier |
| **Combinaison** | ✅ Annotations simples + Service pour complexe | — |

**Notre stratégie** :
- **DTO annotations** : Validation basique immédiate (required, format, range)
- **Service** : Validation complexe (business rules, dépendances, seuils)

```java
// DTO = validation simple & syntaxique
public record QuoteRequestDto(
    @NotNull Long clientId,           // simple notNull
    @NotNull Long produitId,          // simple notNull
    @NotNull @Min(0) Double montant   // simple min
) {}

// Service = validation complexe & métier
@Service
public class QuoteService {
    public Devis createQuote(...) {
        // 1. Client existe ? (404)
        // 2. Produit existe ? (404)
        // 3. Montant valide (>= 0) ? (422)
        // 4. Client a pas trop de devis ? (422)
        // 5. Déterminer statut auto (APPROVED vs PENDING_MANAGER)
    }
}
```

---

---

## 📁 Structure du projet

```
src/main/java/com/baridmedia/assuranceapi/
├── AssuranceApiApplication.java          # Point d'entrée Spring Boot
├── controller/                            # Couche REST
│   ├── ClientController.java             # Endpoints /api/clients
│   ├── ProductController.java            # Endpoints /api/products
│   ├── QuoteController.java              # Endpoints /api/quotes
│   └── ContractController.java           # Endpoints /api/contracts
├── service/                               # Couche métier
│   ├── ClientService.java                # Logique Client
│   ├── ProductService.java               # Logique Produit
│   ├── QuoteService.java                 # Logique Devis + seuils approbation
│   └── ContractService.java              # Logique Contrat
├── domain/                                # Entités JPA
│   ├── Client.java                       # Entity Client
│   ├── Produit.java                      # Entity Produit
│   ├── Devis.java                        # Entity Devis (Quote)
│   ├── Contrat.java                      # Entity Contrat (Contract)
│   ├── QuoteStatus.java                  # Enum : DRAFT, PENDING_MANAGER, APPROVED, REJECTED
│   └── ContractStatus.java               # Enum : ACTIVE, SUSPENDED, EXPIRED, CANCELLED
├── dto/                                   # DTOs et mappings
│   ├── ClientRequestDto.java             # DTO entrée créer Client
│   ├── ClientDto.java                    # DTO sortie Client
│   ├── ProduitRequestDto.java            # DTO entrée créer Produit
│   ├── ProduitDto.java                   # DTO sortie Produit
│   ├── QuoteRequestDto.java              # DTO entrée créer Devis
│   ├── QuoteDto.java                     # DTO sortie Devis
│   ├── ContractRequestDto.java           # DTO entrée créer Contrat
│   └── ContractDto.java                  # DTO sortie Contrat
├── repository/                            # Couche persistence (JPA)
│   ├── ClientRepository.java             # JpaRepository<Client, Long>
│   ├── ProduitRepository.java            # JpaRepository<Produit, Long>
│   ├── DevisRepository.java              # JpaRepository<Devis, Long>
│   └── ContratRepository.java            # JpaRepository<Contrat, Long>
├── exception/                             # Exceptions + handler global
│   ├── ConflictException.java            # Conflit (409)
│   ├── BusinessException.java            # Erreur métier (422)
│   ├── ResourceNotFoundException.java    # Pas trouvé (404)
│   └── GlobalExceptionHandler.java       # @ControllerAdvice centralisé
└── resources/
    ├── application.properties             # Config (H2, JPA, logging)
    └── application-prod.properties       # Config production (PostgreSQL, etc.)
```



**Dernière mise à jour** : 23 juin 2026  
**Version API** : 1.0  

