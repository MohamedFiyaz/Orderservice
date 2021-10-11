package com.casestudy.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private String status;
    private double totalPrice;

    /*@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("orderId")
    private List<OrderedItem> items;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("order_id")
    private Payment payment;*/

    /*public Order(Long orderId, Long customerId, Long restaurantId, String status, double totalPrice) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.totalPrice = totalPrice;
    }*/
}
