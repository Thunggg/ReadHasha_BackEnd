package com.example.thuan.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class VNPayConfig {
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_CURR_CODE = "VND";
    public static final String VNP_LOCALE = "vn";
    // Cập nhật URL callback nếu cần thiết - đảm bảo URL này có thể truy cập được từ
    // VNPay
    public static final String VNP_RETURN_URL = "http://localhost:8080/api/payment/vnpay-payment-callback";

    // Thông tin merchant VNPay
    public static final String VNP_TMN_CODE = "QSFA8YF4";
    public static final String VNP_HASH_SECRET = "EHW4GCGAXOO5Z452OJ2KGFYE4R1M76HO";

    public static final String VNP_API_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_CHECKOUT_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // Múi giờ Việt Nam (GMT+7) - đảm bảo đúng định dạng
    public static final String VNP_TIME_ZONE = "Asia/Ho_Chi_Minh";

    // Thời gian tối đa cho giao dịch (15 phút = 15*60 giây)
    public static final int VNP_EXPIRE_TIME = 15 * 60;

    // Mã phản hồi thành công
    public static final String VNP_RESPONSE_CODE_SUCCESS = "00";

    @Bean
    public Map<String, String> vnpayParameters() {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNP_VERSION);
        vnp_Params.put("vnp_Command", VNP_COMMAND);
        vnp_Params.put("vnp_TmnCode", VNP_TMN_CODE);
        vnp_Params.put("vnp_CurrCode", VNP_CURR_CODE);
        vnp_Params.put("vnp_Locale", VNP_LOCALE);
        vnp_Params.put("vnp_ReturnUrl", VNP_RETURN_URL);
        return vnp_Params;
    }
}