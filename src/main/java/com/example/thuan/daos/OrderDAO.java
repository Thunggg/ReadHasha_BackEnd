package com.example.thuan.daos;

import java.util.List;
import java.util.Map;

import com.example.thuan.models.OrderDTO;

public interface OrderDAO {
    void save(OrderDTO orderDTO);

    OrderDTO find(int orderID);

    void update(OrderDTO orderDTO);

    void delete(int orderID);

    List<OrderDTO> findAll();

    List<OrderDTO> searchByOrderId(int orderID);

    // Phương thức mới để tìm đơn hàng theo username
    List<OrderDTO> findByUsername(String username);

    List<OrderDTO> getOrders(int offset, int pageSize, String whereClause, String sort, Map<String, Object> parameters);

    long countOrdersWithConditions(String whereClause, Map<String, Object> parameters);
}
