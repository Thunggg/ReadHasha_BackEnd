package com.example.thuan.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.example.thuan.request.IntrospectRequest;
import com.example.thuan.ultis.JwtUtil;
import org.springframework.context.annotation.Lazy;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

@Component
public class CustomerJwtDecoder implements JwtDecoder {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Autowired
    @Lazy // Trì hoãn khởi tạo JwtUtil để tránh vòng lặp
    private JwtUtil jwtUtill;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var response = jwtUtill.introspect(IntrospectRequest
                    .builder()
                    .token(token)
                    .build());

            if (!response.isValid())
                throw new Exception("Token invalid");
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();

        }
        return nimbusJwtDecoder.decode(token);
    }
}