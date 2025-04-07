package org.example.casestudy.controller;

import org.example.casestudy.entities.Asset;
import org.example.casestudy.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AssetControllerTest {

    @Mock
    private AssetService assetService;

    @InjectMocks
    private AssetController assetController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAssetsByCustomerId_Success() {
        UUID customerId = UUID.randomUUID();
        List<Asset> expectedAssets = new ArrayList<>();
        expectedAssets.add(new Asset());
        expectedAssets.add(new Asset());

        when(assetService.listAssets(customerId)).thenReturn(expectedAssets);

        List<Asset> actualAssets = assetController.getAssetsByCustomerId(customerId);

        assertEquals(expectedAssets.size(), actualAssets.size());
        verify(assetService, times(1)).listAssets(customerId);
    }
}