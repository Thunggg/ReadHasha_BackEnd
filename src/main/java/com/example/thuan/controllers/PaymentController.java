package com.example.thuan.controllers;

import com.example.thuan.configs.VNPayConfig;
import com.example.thuan.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(
            @RequestParam("orderId") long orderId,
            @RequestParam("amount") long amount,
            @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {

        try {
            String ipAddress = request.getRemoteAddr();
            String paymentUrl = vnPayService.createPaymentUrl(orderId, amount, orderInfo, ipAddress);

            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);

            return ResponseEntity.ok(response);
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating payment URL");
        }
    }

    @GetMapping("/vnpay-payment-callback")
    public void vnpayCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> vnp_Params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && paramValue.length() > 0) {
                vnp_Params.put(paramName, paramValue);
            }
        }

        // Validate payment response
        boolean isValidSignature = vnPayService.validatePaymentResponse(vnp_Params);
        String frontendBaseUrl = "http://localhost:3001/order";
        StringBuilder redirectUrl = new StringBuilder(frontendBaseUrl);

        if (isValidSignature) {
            String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode");

            if (VNPayConfig.VNP_RESPONSE_CODE_SUCCESS.equals(vnp_ResponseCode)) {
                // Payment successful
                String orderId = vnp_Params.get("vnp_TxnRef");
                String transactionId = vnp_Params.get("vnp_TransactionNo");
                String paymentAmount = vnp_Params.get("vnp_Amount");

                // TODO: Update your order status in the database

                redirectUrl.append("?status=success")
                        .append("&orderId=").append(orderId)
                        .append("&transactionId=").append(transactionId)
                        .append("&amount=").append(paymentAmount);
            } else {
                // Payment failed
                redirectUrl.append("?status=failed")
                        .append("&responseCode=").append(vnp_ResponseCode);
            }
        } else {
            // Invalid signature
            redirectUrl.append("?status=error")
                    .append("&message=Invalid+signature");
        }

        response.sendRedirect(redirectUrl.toString());
    }

    @GetMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && paramValue.length() > 0) {
                vnp_Params.put(paramName, paramValue);
            }
        }

        // Validate payment response
        boolean isValidSignature = vnPayService.validatePaymentResponse(vnp_Params);
        String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode");

        Map<String, Object> response = new HashMap<>();

        if (isValidSignature) {
            if (VNPayConfig.VNP_RESPONSE_CODE_SUCCESS.equals(vnp_ResponseCode)) {
                // Thanh toán thành công - cập nhật trạng thái đơn hàng trong DB
                // String orderId = vnp_Params.get("vnp_TxnRef");

                response.put("status", "success");
                response.put("message", "Thanh toán thành công");
                response.put("data", vnp_Params);
            } else {
                response.put("status", "failed");
                response.put("message", "Thanh toán thất bại");
                response.put("responseCode", vnp_ResponseCode);
            }
        } else {
            response.put("status", "error");
            response.put("message", "Chữ ký không hợp lệ");
        }

        return ResponseEntity.ok(response);
    }
}