package com.example.thuan.ultis;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.nimbusds.jose.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;

import com.nimbusds.jwt.SignedJWT;
import com.example.thuan.daos.AccountDAO;
import com.example.thuan.daos.InvalidatedTokenDAO;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.models.InvalidatedTokenDTO;
import com.example.thuan.request.IntrospectRequest;
import com.example.thuan.request.RefreshToken;
import com.example.thuan.respone.AuthenticationResponse;
import com.example.thuan.respone.IntrospectResponse;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.UUID;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration-time}")
    private long EXPIRATION_TIME;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    @Autowired
    @Lazy // Trì hoãn khởi tạo AccountDAO để tránh vòng lặp
    AccountDAO accountDAO;

    @Autowired
    // @Lazy
    private InvalidatedTokenDAO invalidatedTokenDAO;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateTokenForRegister(String email) {
        return Jwts.builder()
                .setSubject(email) // Chỉ lưu email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // // Tạo Access Token với username làm subject
    // public String generateAccessToken(String username) {
    // return Jwts.builder()
    // .setSubject(username) // lưu username vào subject
    // .setIssuedAt(new Date())
    // .setExpiration(new Date(System.currentTimeMillis() +
    // ACCESS_TOKEN_EXPIRATION))
    // .signWith(getSigningKey(), SignatureAlgorithm.HS256)
    // .compact();
    // }

    // build role method
    public String buildScope(AccountDTO accountDTO) {
        if (accountDTO.getRole() == 0)
            return "ADMIN";
        else if (accountDTO.getRole() == 1)
            return "CUSTOMER";
        else
            return "UNKNOWN";
    }

    public String generateAccessToken(AccountDTO accountDTO) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(accountDTO.getUsername())
                .issuer("ReadHasha")
                .claim("scope", buildScope(accountDTO))
                .claim("status", accountDTO.getAccStatus())
                .claim("email", accountDTO.getEmail())
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .expirationTime(
                        new Date(Instant.now().plus(ACCESS_TOKEN_EXPIRATION, ChronoUnit.SECONDS).toEpochMilli()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jwsObject.serialize();
    }

    public String generateRefreshToken(AccountDTO accountDTO) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(accountDTO.getUsername()) // Username làm subject
                .issuer("ReadHasha") // Ứng dụng phát hành token
                .issueTime(new Date()) // Thời gian phát hành
                .jwtID(UUID.randomUUID().toString()) // ID token duy nhất
                .expirationTime(
                        new Date(Instant.now().plus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.MILLIS).toEpochMilli())) // Hết
                                                                                                                  // hạn
                                                                                                                  // 7
                                                                                                                  // ngày
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jwsObject.serialize();
    }

    // veriry token
    public SignedJWT verifyToken(String token, boolean isRefresh) throws Exception {
        JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                        .toInstant().plus(ACCESS_TOKEN_EXPIRATION, ChronoUnit.DAYS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date())))
            throw new Exception("Unauthenticated");

        if (invalidatedTokenDAO.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new Exception("Unauthenticated");

        return signedJWT;
    }

    // check valid token
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            isValid = false;
        }
        return IntrospectResponse.builder()
                .isValid(isValid)
                .build();
    }

    public String validateToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7); // Bỏ "Bearer "
            }

            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // Chỉ lấy email từ subject
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token đã hết hạn!");
        } catch (Exception e) {
            throw new RuntimeException("Token không hợp lệ!");
        }
    }

    // refresh_token
    public AuthenticationResponse refreshToken(RefreshToken request) throws Exception {
        // Kiểm tra token có bị thu hồi không
        if (invalidatedTokenDAO.existsById(request.getToken())) {
            throw new Exception("Refresh token is revoked!");
        }

        var signJWT = verifyToken(request.getToken(), true);
        var jit = signJWT.getJWTClaimsSet().getJWTID();
        var username = signJWT.getJWTClaimsSet().getSubject();

        var account = accountDAO.findByUsername(username);
        if (account == null) {
            throw new Exception("User not found!");
        }

        // Xóa refreshToken cũ trước khi cấp token mới
        invalidatedTokenDAO.deleteByITID(jit);

        // Cấp token mới
        String newAccessToken = generateAccessToken(account);
        String newRefreshToken = generateRefreshToken(account);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticate(true)
                .build();
    }

}
