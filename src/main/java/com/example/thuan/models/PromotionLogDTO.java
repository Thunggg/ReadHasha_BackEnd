// package com.example.thuan.models;

// import jakarta.persistence.*;
// import lombok.*;

// import java.io.Serializable;
// import java.util.Date;

// @NoArgsConstructor
// @AllArgsConstructor
// @Getter
// @Setter
// @ToString
// @Entity
// @Table(name = "promotion_log")
// public class PromotionLogDTO implements Serializable {
// private static final long serialVersionUID = 1L;
// @Id
// @GeneratedValue(strategy = GenerationType.IDENTITY)
// @Basic(optional = false)
// @Column(name = "pro_log_id")
// private Integer proLogId;

// @Column(name = "pro_action")
// private Integer proAction;

// @JoinColumn(name = "pro_id", referencedColumnName = "proID")
// @ManyToOne
// private PromotionDTO proId;

// @JoinColumn(name = "staff_id", referencedColumnName = "staffID")
// @ManyToOne
// private StaffDTO staffId;

// @Column(name = "pro_log_date")
// @Temporal(TemporalType.DATE)
// private Date proLogDate;
// }