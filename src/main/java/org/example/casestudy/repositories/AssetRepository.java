package org.example.casestudy.repositories;

import org.example.casestudy.entities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByCustomerId(UUID customerId);
    Optional<Asset> findByCustomerIdAndAssetName(UUID customerId, String assetName);}
