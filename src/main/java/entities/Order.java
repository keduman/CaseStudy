package entities;

import enums.OrderStatus;
import enums.OrderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String customerId;

    private String assetName;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private Double size;

    private Double price;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private Date createdDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asset> assets;

}
