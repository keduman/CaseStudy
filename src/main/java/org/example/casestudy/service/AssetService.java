package org.example.casestudy.service;

import jakarta.transaction.Transactional;
import org.example.casestudy.entities.Asset;
import org.example.casestudy.repositories.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> listAssets(UUID customerId) {
        return assetRepository.findByCustomerId(customerId);
    }
}
