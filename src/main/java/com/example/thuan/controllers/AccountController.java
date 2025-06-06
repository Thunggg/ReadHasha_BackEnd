package com.example.thuan.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.example.thuan.daos.AccountDAO;
import com.example.thuan.daos.AccountDAOImpl;
import com.example.thuan.exceptions.AppException;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.models.CartDTO;
import com.example.thuan.request.ChangePasswordRequest;
import com.example.thuan.request.UpdateUserRequest;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.respone.Meta;
import com.example.thuan.respone.PaginationResponse;
import com.example.thuan.ultis.ErrorCode;
import com.example.thuan.ultis.JwtUtil;
import com.example.thuan.ultis.Status;

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
            @RequestPart("register") String account,
            HttpServletResponse response) {
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
                        .body(BaseResponse.error("Không thể gửi lại OTP. Tài khoản đã được xác thực hoặc bị khóa!",
                                400,
                                "Invalid Account"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Gửi lại OTP thất bại", 500, e.getMessage()));
        }
    }

    @GetMapping("/fetch-account")
    public BaseResponse<AccountDTO> getAccount(@RequestHeader("Authorization") String token) {
        try {
            // Giải mã JWT để lấy username
            String userName = jwtUtil.validateToken(token);
            AccountDTO account = accountDAO.findByUsername(userName);

            if (account == null) {
                return BaseResponse.error(ErrorCode.TOKEN_EXPIRED.getMessage(), ErrorCode.TOKEN_EXPIRED.getCode(),
                        null);
            }

            // Trả về trực tiếp account, không cần wrap trong Map nữa
            return BaseResponse.success("Lấy account thành công!", 200, account, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("Lỗi hệ thống", 500, e.getMessage());
        }
    }

    @GetMapping("/account-pagination")
    public BaseResponse<PaginationResponse<AccountDTO>> getAccounts(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "userName", required = false) String userName,
            @RequestParam(name = "startDob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDob,
            @RequestParam(name = "endDob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDob,
            @RequestParam(name = "accStatus", required = false) Integer accStatus,
            @RequestParam(name = "current", defaultValue = "1") int current,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sort", required = false) String sort) {

        try {
            int offset = (current - 1) * pageSize;

            // Gọi DAO với các tham số tìm kiếm
            List<AccountDTO> data = accountDAO.getAccounts(offset, pageSize, email, userName, startDob, endDob, sort,
                    accStatus);

            // Đếm tổng số bản ghi theo điều kiện tìm kiếm
            long total = accountDAO.countAccountsWithConditions(email, userName, startDob, endDob, accStatus)
                    .size();

            int pages = (pageSize == 0) ? 0 : (int) Math.ceil((double) total / pageSize);

            Meta meta = new Meta();
            meta.setCurrent(current);
            meta.setPageSize(pageSize);
            meta.setPages(pages);
            meta.setTotal(total);

            PaginationResponse<AccountDTO> pagingRes = new PaginationResponse<>(data, meta);
            return BaseResponse.success("Lấy danh sách account thành công!", 200, pagingRes, null, null);
        } catch (AppException e) {
            return BaseResponse.error(e.getMessage(), e.getErrorCode().getCode(), null);
        }
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<BaseResponse<String>> deleteUserByUsername(
            @RequestParam(name = "username") String username) {
        try {
            // Kiểm tra xem người dùng có tồn tại và không phải là admin
            boolean isDeleted = accountDAO.deleteUserByUsername(username);

            if (isDeleted) {
                return ResponseEntity.ok()
                        .body(BaseResponse.success("Xóa người dùng thành công!", 200, null, null, null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(BaseResponse.error("Không thể xóa người dùng. Người dùng không tồn tại hoặc là admin.",
                                400,
                                "Invalid Request"));
            }
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Xóa người dùng thất bại", 500, e.getMessage()));
        }
    }

    @PutMapping("/update-user")
    public ResponseEntity<BaseResponse<AccountDTO>> updateUser(
            @RequestBody UpdateUserRequest updateRequest) {
        try {
            AccountDTO updatedUser = accountDAO.updateUser(updateRequest);

            return ResponseEntity.ok()
                    .body(BaseResponse.success(
                            "Cập nhật thành công",
                            200,
                            updatedUser,
                            null,
                            null));

        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(BaseResponse.error(e.getMessage(), e.getErrorCode().getCode(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Lỗi server", 500, e.getMessage()));
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<BaseResponse<String>> updatePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String token) {

        try {
            // Lấy username từ token
            String username = jwtUtil.validateToken(token);

            // Gọi DAO để xử lý đổi mật khẩu
            boolean isUpdated = accountDAO.changePassword(username, request);

            if (isUpdated) {
                return ResponseEntity.ok()
                        .body(BaseResponse.success("Cập nhật mật khẩu thành công", 200, null, null, null));
            } else {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("Cập nhật mật khẩu thất bại", 400, "Invalid Request"));
            }

        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(BaseResponse.error(e.getMessage(), e.getErrorCode().getCode(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Lỗi server", 500, e.getMessage()));
        }
    }

    @GetMapping("/check-email")
    public BaseResponse<AccountDTO> checkEmailExists(
            @RequestParam("email") String email) {
        try {
            AccountDTO exists = accountDAO.findByEmail(email);

            // Trường hợp không tìm thấy email
            if (exists == null) {
                return BaseResponse.error("Email không tồn tại", 404, null);
            }

            // Trường hợp tài khoản bị khóa
            if (exists.getAccStatus() == Status.INACTIVE_STATUS.getValue()) {
                return BaseResponse.error("Tài khoản đã bị khóa", 403, null);
            }

            // Trường hợp hợp lệ
            return BaseResponse.success("Tồn tại tài khoản", 200, exists, null, null);

        } catch (Exception e) {
            return BaseResponse.error("Lỗi hệ thống", 500, e.getMessage());
        }
    }

    // AccountController.java
    @PostMapping("/email/send-otp")
    public ResponseEntity<BaseResponse<String>> sendVerificationOTP(
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String accessToken = jwtUtil.generateTokenForRegister(email);

            // Validate email
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("Email không được để trống", 400, null));
            }

            // Gọi DAO
            boolean success = accountDAO.sendVerificationOTP(email);

            if (success) {
                return ResponseEntity.ok()
                        .body(BaseResponse.success("Mã OTP đã được gửi", 200, null, accessToken, null));
            } else {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("Không thể gửi OTP", 400, "Tài khoản không hợp lệ"));
            }

        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(BaseResponse.error(e.getMessage(), e.getErrorCode().getCode(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Lỗi server", 500, e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<String>> resetPassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        try {
            // 1. Giải mã JWT để lấy email
            String email = jwtUtil.validateToken(token);

            // 2. Validate input
            String newPassword = request.get("newPassword");

            // 3. Gọi DAO reset password
            boolean success = accountDAO.resetPassword(email, newPassword);

            if (success) {
                return ResponseEntity.ok()
                        .body(BaseResponse.success("Đặt lại mật khẩu thành công", 200, null, null, null));
            }

            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("Đặt lại mật khẩu thất bại", 400, null));

        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(BaseResponse.error(e.getMessage(), e.getErrorCode().getCode(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("Lỗi hệ thống", 500, e.getMessage()));
        }
    }
}
