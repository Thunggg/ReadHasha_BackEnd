package com.example.thuan.request;

import java.sql.Date;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private Date dob;
    private String phone;
    private String address;
    private Integer sex;
    private Integer accStatus;
    private String username;
}
