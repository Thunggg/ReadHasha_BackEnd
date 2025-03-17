package com.example.thuan.daos;

import com.example.thuan.exceptions.AppException;
import com.example.thuan.exceptions.AuthenticationException;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.models.InvalidatedTokenDTO;
import com.example.thuan.request.AuthenticationRequest;
import com.example.thuan.request.LogoutRequest;
import com.example.thuan.respone.AuthenticationResponse;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.ultis.ErrorCode;
import com.example.thuan.ultis.JwtUtil;
import com.example.thuan.ultis.Status;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.text.ParseException;

import java.util.Date;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationDAO {

    AccountDAO accountDAO;
    JwtUtil jwtUtil;
    InvalidatedTokenDAO invalidatedTokenDAO;

    // authenticate (đăng nhập)
    public BaseResponse<AuthenticationResponse> authenticate(AuthenticationRequest request,
            HttpServletResponse response) {
        AccountDTO account = accountDAO.findByUsername(request.getUsername());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        if (account == null || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new AuthenticationException(
                    ErrorCode.USER_OR_PASSWORD_WRONG.getMessage(),
                    HttpStatus.NOT_FOUND,
                    ErrorCode.USER_OR_PASSWORD_WRONG.getCode());
        }

        if (account.getAccStatus() == Status.INACTIVE_STATUS.getValue()) {
            throw new AuthenticationException(
                    ErrorCode.USER_INACTIVE_STATUS.getMessage(),
                    HttpStatus.FORBIDDEN,
                    ErrorCode.USER_INACTIVE_STATUS.getCode());
        }

        if (account.getAccStatus() == Status.UNVERIFIED_STATUS.getValue()) {
            String token = jwtUtil.generateTokenForRegister(account.getEmail());

            return BaseResponse.error_for_login_again(ErrorCode.USER_UNVERIFIED_STATUS.getMessage(),
                    403, null, token);
        }

        String access_token = jwtUtil.generateAccessToken(account);
        String refresh_token = jwtUtil.generateRefreshToken(account);

        // add refresh_token vào cookie
        Cookie cookie = new Cookie("refresh_token", refresh_token);
        cookie.setMaxAge(60 * 60 * 24 * 30); // 30 ngày
        cookie.setHttpOnly(true); // Bảo mật hơn, không thể truy cập từ JavaScript
        cookie.setSecure(false); // Chỉ gửi qua HTTPS
        cookie.setPath("/"); // Áp dụng cho toàn bộ domain
        response.addCookie(cookie); // ✅ Gửi cookie về client

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken(access_token)
                .refreshToken(null)
                .account(account)
                .authenticate(true)
                .build();

        return BaseResponse.success("Đăng nhập thành công", 200, authResponse, null, null);
    }

    // Xử lý logout
    @Transactional
    public void logout(LogoutRequest request, HttpServletResponse response) {
        try {

            // Xác minh token (chỉ chấp nhận refresh token)
            SignedJWT signedToken = jwtUtil.verifyToken(request.getToken(), true);

            // Lấy JWT ID (jit) và thời gian hết hạn
            String jit = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            // Kiểm tra nếu token đã bị thu hồi trước đó
            if (invalidatedTokenDAO.existsById(jit)) {
                throw new AppException(ErrorCode.TOKEN_REVOKED);
            }

            // Lưu token bị thu hồi vào database
            InvalidatedTokenDTO invalidatedTokenDTO = InvalidatedTokenDTO.builder()
                    .ITID(jit)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenDAO.save(invalidatedTokenDTO);

            // Xóa cookie bằng cách đặt thời gian sống = 0
            Cookie cookie = new Cookie("refresh_token", null);
            cookie.setMaxAge(0); // ✅ Đặt maxAge=0 để xóa ngay
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Phù hợp với localhost (HTTP)
            cookie.setPath("/");
            cookie.setDomain("localhost");
            response.addCookie(cookie);

        } catch (ParseException e) {
            log.error("Invalid token format: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_TOKEN);
        } catch (AppException e) {
            throw new AppException(e.getErrorCode());
        } catch (Exception e) {
            throw new AppException(ErrorCode.WRONG_TOKEN);
        }
    }

}
