package org.example.casestudy.service;

import org.example.casestudy.entities.Asset;
import org.example.casestudy.repositories.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listAssets_Success() {
        UUID customerId = UUID.randomUUID();
        List<Asset> expectedAssets = new ArrayList<>();
        expectedAssets.add(new Asset());
        expectedAssets.add(new Asset());

        when(assetRepository.findByCustomerId(customerId)).thenReturn(expectedAssets);

        List<Asset> actualAssets = assetService.listAssets(customerId);

        assertEquals(expectedAssets.size(), actualAssets.size());
        assertEquals(expectedAssets, actualAssets);
        verify(assetRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void listAssets_NoAssetsFound() {
        UUID customerId = UUID.randomUUID();
        List<Asset> expectedAssets = new ArrayList<>(); // Empty list

        when(assetRepository.findByCustomerId(customerId)).thenReturn(expectedAssets);

        List<Asset> actualAssets = assetService.listAssets(customerId);

        assertEquals(expectedAssets.size(), actualAssets.size()); // Should be 0
        assertTrue(actualAssets.isEmpty());
        verify(assetRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void listAssets_ExceptionScenario() {
        UUID customerId = UUID.randomUUID();

        when(assetRepository.findByCustomerId(customerId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> assetService.listAssets(customerId));
        verify(assetRepository, times(1)).findByCustomerId(customerId);
    }
}