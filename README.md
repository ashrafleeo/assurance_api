# AssuranceApi — API REST de Gestion d'Assurance

Une API REST Spring Boot moderne pour gérer les devis d'assurance, les contrats, les clients et les produits.


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
                │ 1
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


**Règles métier** :
- Email est obligatoire et unique (409 Conflict si doublon)
- Le client peut avoir plusieurs devis

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


**Règles métier** :
- Code est obligatoire et unique (409 Conflict si doublon)
- Un produit peut être utilisé dans plusieurs devis

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


**Statuts du devis** :

```
DRAFT → PENDING_MANAGER → APPROVED → [Contract créé] ou REJECTED
        ↓
    [si montant ≤ 10k → auto APPROVED]
    [si montant > 10k → PENDING_MANAGER (approb manuelle)]
```


**Règles métier (critique)** :
- **Auto-approbation** : Si montant ≤ 10 000 → statut APPROVED immédiatement
- **Approbation manuelle** : Si montant > 10 000 → statut PENDING_MANAGER (nécessite appel `/approve`)
- Client et Produit doivent exister (404 sinon)
- Montant doit être ≥ 0 (422 sinon)

---

### 4. Contrat (Entité)

**Responsabilité** : Représenter un contrat d'assurance actif

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




**Dernière mise à jour** : 23 juin 2026  
**Version API** : 1.0  

