package com.baridmedia.assuranceapi.repository;

import com.baridmedia.assuranceapi.domain.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
	boolean existsByCode(String code);

}

