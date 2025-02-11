package com.example.thuan.respone;

import com.example.thuan.models.AccountDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    private AccountDTO account;
    private String accessToken;
    private String refreshToken;
}
