package com.example.thuan.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;
import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Builder
@Table(name = "account")
public class AccountDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "username")
    private String username;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "password")
    private String password;

    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    private Date dob;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "role")
    private Integer role;

    @Column(name = "address")
    private String address;

    @Column(name = "sex")
    private Integer sex;

    @Column(name = "accstatus")
    private Integer accStatus;

    @Column(name = "code")
    private String code;

    // Ignoring lazy-loaded collections that don’t need serialization
    @OneToMany(mappedBy = "username", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<OrderDTO> orderDTOCollection;

    // @OneToMany(mappedBy = "username", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private Collection<StaffDTO> staffDTOCollection;

    @OneToMany(mappedBy = "username", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Collection<CartDTO> cartCollection;

    // @OneToMany(mappedBy = "username")
    // @JsonIgnore
    // private Collection<InvalidatedTokenDTO> invalidatedTokenCollection;
}