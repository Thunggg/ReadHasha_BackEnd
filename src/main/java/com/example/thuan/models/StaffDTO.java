package com.example.thuan.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "staff")
public class StaffDTO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "staffid")
    private Integer staffID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private AccountDTO username;

    // Ignoring other collections that don’t need serialization
    // @OneToMany(mappedBy = "staffID", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<OrderDTO> orderList;

    // @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<PromotionDTO> createByList;

    // @OneToMany(mappedBy = "approvedBy", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<PromotionDTO> approvedByList;

    // @OneToMany(mappedBy = "staffID", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<ImportStockDTO> importStockList;

    // @OneToMany(mappedBy = "staffID", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<NotificationDTO> notificationList;
}
