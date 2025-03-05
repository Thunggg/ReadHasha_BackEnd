package com.example.thuan.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "orderdetail")
public class OrderDetailDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "odid")
    private Integer odid;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "bookid")
    private BookDTO bookID;

    @JoinColumn(name = "orderid", referencedColumnName = "orderid")
    @ManyToOne
    @JsonBackReference
    private OrderDTO orderID;

    @Column(name = "totalprice")
    private BigDecimal totalPrice;

}