package org.example.casestudy.controller;

import org.example.casestudy.entities.Asset;
import org.example.casestudy.entities.Customer;
import org.example.casestudy.service.AssetService;
import org.example.casestudy.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;
    private final UserUtils userUtils;

    @Autowired
    public AssetController(AssetService assetService, UserUtils userUtils) {
        this.assetService = assetService;
        this.userUtils = userUtils;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Asset> getAssetsByCustomerId(@PathVariable UUID id) {
        return assetService.listAssets(id);
    }

    @GetMapping
    public List<Asset> getAssetsByCustomer() {
       Customer pr = (Customer) userUtils.getPrincipal();
       return assetService.listAssets(pr.getId());
    }


}
