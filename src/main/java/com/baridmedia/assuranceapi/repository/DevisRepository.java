package com.baridmedia.assuranceapi.repository;

import com.baridmedia.assuranceapi.domain.Devis;
import com.baridmedia.assuranceapi.domain.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {
    List<Devis> findByStatut(QuoteStatus statut);
}

