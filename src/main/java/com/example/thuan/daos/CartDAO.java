package com.example.thuan.daos;

import java.util.List;

import com.example.thuan.models.CartDTO;

public interface CartDAO {
    void addBookToCart(String username, int bookID, int quantity);

    List<CartDTO> viewCart(String username);

    void editQuantity(String username, int bookID, int quantity);

    void deleteBookFromCart(String username, int cartID);
}