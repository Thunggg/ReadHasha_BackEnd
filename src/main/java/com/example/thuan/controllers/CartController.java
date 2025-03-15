package com.example.thuan.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.example.thuan.daos.CartDAO;
import com.example.thuan.models.CartDTO;
import com.example.thuan.respone.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartDAO cartDAO;

    @Autowired
    public CartController(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @PostMapping("/add")
    public BaseResponse<CartDTO> addBookToCart(@RequestParam(name = "username") String username,
            @RequestParam(name = "bookID") int bookID,
            @RequestParam(name = "quantity") int quantity) {
        try {
            cartDAO.addBookToCart(username, bookID, quantity);
            return BaseResponse.success("Thêm sách vào giỏ hàng thành công!.", 200, null, null, null);
        } catch (Exception e) {
            return BaseResponse.error(e.getMessage(), 500, null);
        }
    }

    @GetMapping("/{username}")
    @Transactional
    public ResponseEntity<List<CartDTO>> viewCart(@PathVariable String username) {
        try {
            System.out.println("Received username: " + username);
            // Log giá trị username
            List<CartDTO> cart = cartDAO.viewCart(username);

            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            System.err.println("Error fetching cart: " + e.getMessage()); // Log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update")
    public BaseResponse<CartDTO> editQuantity(@RequestParam(name = "username") String username,
            @RequestParam(name = "bookID") int bookID,
            @RequestParam(name = "quantity") int quantity) {
        try {
            if (quantity < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1.");
            }
            cartDAO.editQuantity(username, bookID, quantity);
            return BaseResponse.success("Cập nhật số lượng sách thành công!.", 200, null, null, null);
        } catch (IllegalArgumentException e) {
            return BaseResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(), null);
        } catch (RuntimeException e) {
            return BaseResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value(), null);
        } catch (Exception e) {
            return BaseResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        }
    }

    @DeleteMapping("/delete")
    public BaseResponse<CartDTO> deleteBookFromCart(@RequestParam(name = "username") String username,
            @RequestParam(name = "bookID") int bookID) {
        try {
            cartDAO.deleteBookFromCart(username, bookID);
            return BaseResponse.success("Xóa sách khỏi giỏ hàng thành công!", 200, null, null, null);
        } catch (RuntimeException e) {
            return BaseResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value(), null);
        } catch (Exception e) {
            return BaseResponse.error("Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

}