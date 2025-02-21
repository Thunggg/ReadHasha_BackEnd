package com.example.thuan.daos;

import com.example.thuan.exceptions.AuthenticationException;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.request.AuthenticationRequest;
import com.example.thuan.respone.AuthenticationResponse;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.ultis.ErrorCode;
import com.example.thuan.ultis.JwtUtil;
import com.example.thuan.ultis.Status;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.time.Instant;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationDAO {

    AccountDAO accountDAO;
    JwtUtil JwtUtil;

    // authenticate
    public BaseResponse<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        AccountDTO account = accountDAO.findByUsername(request.getUsername());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        if (account == null || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new AuthenticationException(
                    ErrorCode.USER_NOT_FOUND.getMessage(),
                    HttpStatus.NOT_FOUND,
                    ErrorCode.USER_NOT_FOUND.getCode());
        }

        if (account.getAccStatus() == Status.INACTIVE_STATUS.getValue()) {
            throw new AuthenticationException(
                    ErrorCode.USER_INACTIVE_STATUS.getMessage(),
                    HttpStatus.FORBIDDEN,
                    ErrorCode.USER_INACTIVE_STATUS.getCode());
        }

        if (account.getAccStatus() == Status.UNVERIFIED_STATUS.getValue()) {
            String token = JwtUtil.generateTokenForRegister(account.getEmail());
            // throw new AuthenticationException(
            // ErrorCode.USER_UNVERIFIED_STATUS.getMessage(),
            // HttpStatus.FORBIDDEN,
            // ErrorCode.USER_UNVERIFIED_STATUS.getCode());

            return BaseResponse.error_for_login_again(ErrorCode.USER_UNVERIFIED_STATUS.getMessage(),
                    403, null, token);
        }

        String access_token = JwtUtil.generateAccessToken(account);
        String refresh_token = JwtUtil.generateRefreshToken(account);

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken(access_token)
                .refreshToken(refresh_token)
                .account(account)
                .authenticate(true)
                .build();

        return BaseResponse.success("Đăng nhập thành công", 200, authResponse, null, null);
    }
}
