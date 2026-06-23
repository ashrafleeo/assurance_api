package com.baridmedia.assuranceapi.repository;

import com.baridmedia.assuranceapi.domain.Contrat;
import com.baridmedia.assuranceapi.domain.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    boolean existsByDevis(Devis devis);
}

