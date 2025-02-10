package com.example.thuan.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.example.thuan.daos.AccountDAO;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.ultis.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

// @Slf4j // cho phép sử dụng log.infor
@RestController
@RequestMapping("/api/v1/accounts")
// @EnableMethodSecurity(prePostEnabled = true)
public class AccountController {

    final int ROLE_ADMIN = 0;
    final int ROLE_CUSTOMER = 1;
    final int ROLE_SELLER_STAFF = 2;
    final int ROLE_WAREHOUSE_STAFF = 3;
    final String DEFAULT_PASSWORD = "12345";
    AccountDAO accountDAO;
    // StaffDAO staffDAO;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    // @Autowired
    // public AccountController(AccountDAO accountDAO, StaffDAO staffDAO) {
    // this.accountDAO = accountDAO;
    // this.staffDAO = staffDAO;
    // }

    @Autowired
    public AccountController(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

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
            String accessToken = jwtUtil.generateToken(createdAccount.getEmail());
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

}
