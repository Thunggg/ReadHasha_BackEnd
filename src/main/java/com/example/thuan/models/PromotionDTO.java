package com.example.thuan.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "promotion")
public class PromotionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "proid")
    private Integer proID;

    @Column(name = "proname")
    private String proName;

    @Column(name = "procode")
    private String proCode;
    // @Max(value=?) @Min(value=?)//if you know range of your decimal fields
    // consider using these annotations to enforce field validation
    @Column(name = "discount")
    private Double discount;

    @Column(name = "startdate")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "enddate")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "prostatus")
    private Integer proStatus;

    @OneToMany(mappedBy = "proID")
    @JsonManagedReference
    @JsonIgnore
    private List<OrderDTO> orderList;

    @JoinColumn(name = "createdby", referencedColumnName = "username")
    @ManyToOne
    private AccountDTO createdBy;

    // @OneToMany(mappedBy = "proId")
    // @JsonIgnore
    // @ToString.Exclude
    // private List<PromotionLogDTO> promotionLogList;
}