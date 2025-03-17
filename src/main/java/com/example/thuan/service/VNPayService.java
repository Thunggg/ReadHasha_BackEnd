package com.example.thuan.service;

import com.example.thuan.configs.VNPayConfig;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    public String createPaymentUrl(long orderId, long amount, String orderInfo, String ipAddress)
            throws UnsupportedEncodingException {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.VNP_VERSION);
        vnp_Params.put("vnp_Command", VNPayConfig.VNP_COMMAND);
        vnp_Params.put("vnp_TmnCode", VNPayConfig.VNP_TMN_CODE);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // Convert to VND (smallest unit)
        vnp_Params.put("vnp_CurrCode", VNPayConfig.VNP_CURR_CODE);

        // Generate transaction reference
        String vnp_TxnRef = String.valueOf(orderId);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other"); // Default order type

        vnp_Params.put("vnp_Locale", VNPayConfig.VNP_LOCALE);
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.VNP_RETURN_URL);
        vnp_Params.put("vnp_IpAddr", ipAddress);

        // Create date format for VNPay
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(VNPayConfig.VNP_TIME_ZONE));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone(VNPayConfig.VNP_TIME_ZONE));
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Thêm thời gian hết hạn cho giao dịch
        Calendar expireDate = Calendar.getInstance(TimeZone.getTimeZone(VNPayConfig.VNP_TIME_ZONE));
        expireDate.add(Calendar.SECOND, VNPayConfig.VNP_EXPIRE_TIME);
        String vnp_ExpireDate = formatter.format(expireDate.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build URL with parameters
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return VNPayConfig.VNP_CHECKOUT_URL + "?" + queryUrl;
    }

    public boolean validatePaymentResponse(Map<String, String> vnp_Params) {
        // Remove vnp_SecureHash from params to validate
        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHashType");

        // Sort params by field name
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        // Build hash data
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // Calculate secure hash
        String calculatedHash = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData.toString());

        // Compare calculated hash with received hash
        return calculatedHash.equals(vnp_SecureHash);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secret_key);
            byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}