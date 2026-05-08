package com.manh.ecom_be.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "fullname", length = 100)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 100)
    private String phoneNumber;

    @Column(name = "address", length = 100)
    private String address;

    private String note;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    private String status;

    @Column(name = "total_money")
    private Float totalMoney;

    @Column(name = "shipping_method")
    private String shippingMethod = "";

    @Column(name = "shipping_address")
    private String shippingAddress = "";

    @Column(name = "shipping_date")
    private String shippingDate;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "payment_method")
    private String paymentMethod = "";

    private Boolean active;

    @Column(name = "vnp_txn_ref")
    private String vnpTxnRef;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderDetails> orderDetails;

    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = true)
    @JsonBackReference
    private Coupon coupon;
}
