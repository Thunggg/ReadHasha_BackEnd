package com.example.thuan.daos;

import java.util.List;

import com.example.thuan.models.OrderDetailDTO;

public interface OrderDetailDAO {
    void save(OrderDetailDTO orderDetailDTO);

    OrderDetailDTO find(int ODID);

    void update(OrderDetailDTO orderDetailDTO);

    void delete(int ODID);

    List<OrderDetailDTO> findAll();

    List<OrderDetailDTO> findByOrderID(int orderID);
}