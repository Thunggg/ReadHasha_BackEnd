package com.example.thuan.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.example.thuan.daos.AccountDAO;
import com.example.thuan.exceptions.AuthenticationException;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.respone.AuthenticationResponse;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.ultis.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

// @Slf4j // cho phép sử dụng log.infor
@RestController
@RequestMapping("/api/v1/accounts")
// @EnableMethodSecurity(prePostEnabled = true)
public class AccountController {

    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private JwtUtil jwtUtil;

    // @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/")
    public List<AccountDTO> getAccounts() {
        List<AccountDTO> list = new ArrayList<>();
        for (AccountDTO accountDTO : accountDAO.findAll()) {
            if (accountDTO.getAccStatus() != null && accountDTO.getAccStatus() > 0) {
                list.add(accountDTO);
            }
        }
        return list;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<BaseResponse<AccountDTO>> registeredAccount(
            @RequestPart("register") String account, HttpServletResponse response) {
        try {
            AccountDTO createdAccount = accountDAO.registerAccount(account, response);
            String accessToken = jwtUtil.generateTokenForRegister(createdAccount.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success("Đăng ký thành công", 201, createdAccount, accessToken, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("Lỗi đăng ký: " + e.getMessage(), 400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Đăng ký thất bại", 500, e.getMessage()));
        }
    }

    @PostMapping("/email/verify")
    public ResponseEntity<BaseResponse<String>> verifyEmail(
            @RequestHeader("Authorization") String token, // JWT từ header
            @RequestBody Map<String, String> requestBody // OTP từ body
    ) {
        String otp = requestBody.get("otp");

        if (otp == null || otp.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("OTP không được để trống!", 400, "Invalid Request"));
        }

        boolean success = accountDAO.verifyEmail(token, otp);
        if (success) {
            return ResponseEntity.ok()
                    .body(BaseResponse.success("Xác thực email thành công!", 200, null, null, null));
        } else {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("Xác thực thất bại! OTP hoặc Token không hợp lệ.", 400,
                            "Invalid OTP or Token"));
        }
    }

    @PostMapping("/email/resend-otp")
    public ResponseEntity<BaseResponse<String>> resendOTP(
            @RequestHeader("Authorization") String token) {
        try {
            // Giải mã JWT để lấy email
            String email = jwtUtil.validateToken(token);

            // Gọi phương thức resendOTP từ DAO
            boolean success = accountDAO.resendOTP(email);

            if (success) {
                return ResponseEntity.ok()
                        .body(BaseResponse.success("Mã OTP đã được gửi lại đến email của bạn.", 200, null, null, null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(BaseResponse.error("Không thể gửi lại OTP. Tài khoản đã được xác thực.", 400,
                                "Invalid Account"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Gửi lại OTP thất bại", 500, e.getMessage()));
        }
    }

    // // ------------------- ĐĂNG NHẬP -------------------
    // // Endpoint đăng nhập: xử lý toàn bộ logic ở DAO
    // @PostMapping("/auth/login")
    // public ResponseEntity<BaseResponse<?>> login(@RequestBody Map<String, String>
    // loginRequest) {
    // String username = loginRequest.get("username");
    // String password = loginRequest.get("password");

    // try {
    // AuthenticationResponse loginResponse = accountDAO.login(username, password);
    // return ResponseEntity.ok(BaseResponse.success(
    // "Đăng nhập thành công",
    // 200,
    // loginResponse.getAccount(),
    // loginResponse.getAccessToken(),
    // loginResponse.getRefreshToken()));
    // } catch (AuthenticationException e) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    // .body(BaseResponse.error(e.getMessage(), 401, "Invalid credentials"));
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(BaseResponse.error("Lỗi hệ thống", 500, e.getMessage()));
    // }
    // }

    // // ------------------- REFRESH TOKEN -------------------
    // // Endpoint làm mới access token: nhận refresh token và trả về access token
    // mới
    // @PostMapping("/refresh")
    // public ResponseEntity<BaseResponse<?>> refreshToken(@RequestBody Map<String,
    // String> tokenRequest) {
    // String refreshToken = tokenRequest.get("refreshToken");
    // try {
    // AuthenticationResponse loginResponse = accountDAO.refreshToken(refreshToken);
    // return ResponseEntity.ok(BaseResponse.success("Token refreshed successfully",
    // 200,
    // loginResponse.getAccount(), loginResponse.getAccessToken(),
    // loginResponse.getRefreshToken()));
    // } catch (RuntimeException e) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    // .body(BaseResponse.error(e.getMessage(), 401, "Invalid refresh token"));
    // }
    // }

}
