package com.example.thuan.daos;

import java.util.List;

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
}
