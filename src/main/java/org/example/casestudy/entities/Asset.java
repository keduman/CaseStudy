package org.example.casestudy.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "asset")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID customerId;
    private String assetName;
    private Long size;
    private Long usableSize;

}
