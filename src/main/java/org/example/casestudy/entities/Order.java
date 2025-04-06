package org.example.casestudy.entities;

import org.example.casestudy.enums.OrderStatus;
import org.example.casestudy.enums.OrderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "`order`")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID customerId;
    private String assetName;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    private Long size;
    private Double price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createDate;

    @PrePersist
    public void prePersist() {
        createDate = LocalDateTime.now();
    }


}
