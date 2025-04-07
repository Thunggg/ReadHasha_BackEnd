package com.example.thuan.controllers;

import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.thuan.daos.AccountDAO;
import com.example.thuan.daos.BookDAO;
import com.example.thuan.daos.OrderDAO;
import com.example.thuan.daos.OrderDetailDAO;
import com.example.thuan.daos.PromotionDAO;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.models.BookDTO;
import com.example.thuan.models.OrderDTO;
import com.example.thuan.models.OrderDetailDTO;
import com.example.thuan.models.PromotionDTO;
import com.example.thuan.request.OrderDetailRequestDTO;
import com.example.thuan.request.OrderRequestDTO;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.respone.Meta;
import com.example.thuan.respone.PaginationResponse;
import com.example.thuan.ultis.EmailSenderUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderDAO orderDAO;
    private final OrderDetailDAO orderDetailDAO;
    private final BookDAO bookDAO;
    private final PromotionDAO promotionDAO;
    private final AccountDAO accountDAO;
    private final EmailSenderUtil emailSenderUtil;
    StringBuilder emailContent = new StringBuilder();

    @Autowired
    public OrderController(OrderDAO orderDAO, OrderDetailDAO orderDetailDAO, BookDAO bookDAO, PromotionDAO promotionDAO,
            AccountDAO accountDAO, EmailSenderUtil emailSenderUtil) {
        this.orderDAO = orderDAO;
        this.orderDetailDAO = orderDetailDAO;
        this.bookDAO = bookDAO;
        this.promotionDAO = promotionDAO;
        this.accountDAO = accountDAO;
        this.emailSenderUtil = emailSenderUtil;
    }

    @GetMapping("/")
    public List<OrderDTO> getAllOrders() {
        List<OrderDTO> orders = orderDAO.findAll();
        for (OrderDTO order : orders) {
            if (order.getUsername() != null) {
                order.setCustomerName(order.getUsername().getUsername()); // Sử dụng username trực tiếp
            }
            if (order.getProID() != null) {
                order.setPromotionCode(order.getProID().getProCode());
            }
        }
        return orders;
    }

    @PostMapping("/")
    @Transactional
    public BaseResponse<Map<String, Object>> createOrder(@RequestBody OrderRequestDTO orderRequest) {
        try {
            // Kiểm tra userDetails != null
            if (orderRequest.getUsername() == null) {
                return BaseResponse.error("User chưa đăng nhập", HttpStatus.UNAUTHORIZED.value(), null);
            }

            // Lấy thông tin người dùng
            AccountDTO account = accountDAO.findByUsername(orderRequest.getUsername());
            if (account == null) {
                return BaseResponse.error("User not found", HttpStatus.UNAUTHORIZED.value(), null);
            }

            // Tạo đơn hàng mới
            OrderDTO newOrder = new OrderDTO();
            newOrder.setOrderDate(new Date(System.currentTimeMillis()));
            newOrder.setOrderStatus(1); // 1: Trạng thái đã đặt hàng
            newOrder.setUsername(account);
            newOrder.setOrderAddress(orderRequest.getAddress());

            // Thêm kiểm tra null cho proID
            if (orderRequest.getPromotionID() != null) {
                newOrder.setProID(promotionDAO.find(orderRequest.getPromotionID()));
            } else {
                newOrder.setProID(null); // Không có promotion
            }

            // Lưu đơn hàng vào database
            orderDAO.save(newOrder);

            // Xử lý từng chi tiết đơn hàng
            for (OrderDetailRequestDTO detailRequest : orderRequest.getDetails()) {
                BookDTO book = bookDAO.find(detailRequest.getBookId());
                if (book == null) {
                    throw new RuntimeException("Book not found with ID: " + detailRequest.getBookId());
                }

                // Kiểm tra số lượng tồn kho
                if (book.getBookQuantity() < detailRequest.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for book ID: " + book.getBookID());
                }

                // Tạo chi tiết đơn hàng
                OrderDetailDTO orderDetail = new OrderDetailDTO();
                orderDetail.setBookID(book);
                orderDetail.setOrderID(newOrder);
                orderDetail.setQuantity(detailRequest.getQuantity());
                orderDetail.setTotalPrice(
                        new BigDecimal(orderRequest.getFinalPrice()));

                // Lưu chi tiết đơn hàng
                orderDetailDAO.save(orderDetail);

                // Cập nhật số lượng tồn kho
                book.setBookQuantity(book.getBookQuantity() - detailRequest.getQuantity());
                bookDAO.update(book);
            }

            // Tạo data trả về chứa orderId
            Map<String, Object> data = new HashMap<>();
            data.put("orderId", newOrder.getOrderID());

            // Giả sử access_token và refresh_token không cần trả về, truyền null
            return BaseResponse.success("Order created successfully", HttpStatus.OK.value(), data, null, null);
        } catch (Exception e) {
            return BaseResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        }
    }

    @GetMapping("/history")
    public BaseResponse<List<OrderDTO>> getOrderHistory(@RequestParam("username") String username) {
        // Kiểm tra user có tồn tại không
        AccountDTO account = accountDAO.findByUsername(username);
        if (account == null) {
            return BaseResponse.error("User not found", HttpStatus.NOT_FOUND.value(), null);
        }
        // Truy vấn lịch sử đơn hàng (bao gồm orderDetailList và thông tin sách) theo
        // username
        List<OrderDTO> orders = orderDAO.findByUsername(username);

        // Populate promotion code
        for (OrderDTO order : orders) {
            if (order.getProID() != null) {
                order.setPromotionCode(order.getProID().getProCode());
            }
        }

        return BaseResponse.success("Order history retrieved successfully", HttpStatus.OK.value(), orders, null, null);
    }

    @GetMapping("/order-pagination")
    @Transactional
    public BaseResponse<PaginationResponse<OrderDTO>> getOrderPagination(
            @RequestParam(name = "orderID", required = false) String orderIDParam,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "orderStatus", required = false) String orderStatus,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "current", defaultValue = "1") int current,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sort", required = false) String sort) {
        try {
            int offset = (current - 1) * pageSize;

            List<String> conditions = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();

            if (orderIDParam != null && !orderIDParam.trim().isEmpty()) {
                try {
                    // Chuyển đổi orderID từ String sang Integer
                    Integer orderID = Integer.parseInt(orderIDParam.trim());
                    conditions.add("o.orderID = :orderID");
                    parameters.put("orderID", orderID);
                } catch (NumberFormatException e) {
                    // Nếu không thể chuyển đổi thành số, bỏ qua điều kiện này
                    System.out.println("Invalid orderID format: " + orderIDParam);
                }
            }

            if (username != null && !username.trim().isEmpty()) {
                conditions.add("LOWER(o.username.username) LIKE LOWER(:username)");
                parameters.put("username", "%" + username.trim() + "%");
            }

            if (orderStatus != null && !orderStatus.trim().isEmpty()) {
                conditions.add("o.orderStatus = :orderStatus");
                parameters.put("orderStatus", Integer.parseInt(orderStatus));
            }

            if (startDate != null && !startDate.trim().isEmpty()) {
                conditions.add("o.orderDate >= :startDate");
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date parsedStartDate = dateFormat.parse(startDate);
                    parameters.put("startDate", parsedStartDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                conditions.add("o.orderDate <= :endDate");
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date parsedEndDate = dateFormat.parse(endDate);
                    parameters.put("endDate", parsedEndDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String whereClause = "";
            if (!conditions.isEmpty()) {
                whereClause = " WHERE " + String.join(" AND ", conditions);
            }

            // Lấy danh sách order theo điều kiện tìm kiếm và sắp xếp
            List<OrderDTO> data = orderDAO.getOrders(offset, pageSize, whereClause, sort, parameters);

            // Đảm bảo orderDetailList được load và thiết lập customerName
            for (OrderDTO order : data) {
                if (order.getOrderDetailList() != null) {
                    order.getOrderDetailList().size(); // Force initialization
                }
                if (order.getUsername() != null) {
                    order.setCustomerName(order.getUsername().getUsername());
                }
                if (order.getProID() != null) {
                    order.setPromotionCode(order.getProID().getProCode());
                }
            }

            // Đếm tổng số bản ghi theo điều kiện tìm kiếm
            long total = orderDAO.countOrdersWithConditions(whereClause, parameters);
            int pages = (pageSize == 0) ? 0 : (int) Math.ceil((double) total / pageSize);

            Meta meta = new Meta();
            meta.setCurrent(current);
            meta.setPageSize(pageSize);
            meta.setPages(pages);
            meta.setTotal(total);

            PaginationResponse<OrderDTO> pagingRes = new PaginationResponse<>(data, meta);
            return BaseResponse.success("Lấy danh sách đơn hàng thành công!", 200, pagingRes, null, null);
        } catch (Exception e) {
            e.printStackTrace(); // In ra lỗi để debug
            return BaseResponse.error("Lỗi: " + e.getMessage(), 500, null);
        }
    }

    @PatchMapping("/{orderId}/approve")
    @Transactional
    public BaseResponse<OrderDTO> approveOrder(
            @PathVariable("orderId") int orderId,
            @RequestParam("username") String adminUsername) {
        try {
            // Gọi phương thức từ DAO để xác nhận đơn hàng
            OrderDTO order = orderDAO.approveOrder(orderId, adminUsername);

            // Gửi email thông báo cho khách hàng về việc đơn hàng đã được xác nhận
            // try {
            // AccountDTO customer = order.getUsername();
            // if (customer != null && customer.getEmail() != null) {
            // // Sử dụng orderID như một thông báo xác nhận
            // String orderIdStr = String.valueOf(order.getOrderID());
            // emailSenderUtil.sendEmail(customer.getEmail(), "Đơn hàng #" + orderIdStr + "
            // đã được xác nhận");
            // }
            // } catch (Exception e) {
            // // Ghi log lỗi nhưng không dừng quy trình nếu việc gửi email thất bại
            // System.err.println("Không thể gửi email xác nhận: " + e.getMessage());
            // }

            // Trả về thông tin đơn hàng đã cập nhật
            return BaseResponse.success("Xác nhận đơn hàng thành công", 200, order, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi xác nhận đơn hàng: " + e.getMessage(), 500, e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    public BaseResponse<OrderDTO> getOrderById(@PathVariable("orderId") int orderId) {
        try {
            // Gọi phương thức từ DAO để lấy chi tiết đơn hàng
            OrderDTO order = orderDAO.getOrderDetails(orderId);
            return BaseResponse.success("Lấy thông tin đơn hàng thành công", 200, order, null, null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi khi lấy thông tin đơn hàng: " + e.getMessage(), 500, null);
        }
    }
}
