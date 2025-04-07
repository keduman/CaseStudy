package org.example.casestudy.controller;

import org.example.casestudy.entities.Asset;
import org.example.casestudy.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/{id}")
    public List<Asset> getAssetsByCustomerId(@PathVariable UUID id) {
        return assetService.listAssets(id);
    }


}
