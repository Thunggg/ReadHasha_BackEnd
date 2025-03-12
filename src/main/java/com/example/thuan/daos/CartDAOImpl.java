package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.AccountDTO;
import com.example.thuan.models.BookDTO;
import com.example.thuan.models.CartDTO;

import java.util.List;

@Repository
public class CartDAOImpl implements CartDAO {

    private final EntityManager entityManager;

    @Autowired
    public CartDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void addBookToCart(String username, int bookID, int quantity) {
        // Tìm trong giỏ hàng với username và bookID
        String jpql = "FROM CartDTO WHERE username.username = :username AND bookID.bookID = :bookID";
        TypedQuery<CartDTO> query = entityManager.createQuery(jpql, CartDTO.class);
        query.setParameter("username", username);
        query.setParameter("bookID", bookID);

        CartDTO cartItem;

        if (!query.getResultList().isEmpty()) {
            // Nếu đã có trong giỏ, cập nhật số lượng
            cartItem = query.getSingleResult();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            entityManager.merge(cartItem);
        } else {
            // Nếu chưa có, thêm mới
            cartItem = new CartDTO();
            cartItem.setUsername(entityManager.find(AccountDTO.class, username));
            cartItem.setBookID(entityManager.find(BookDTO.class, bookID));
            cartItem.setQuantity(quantity);
            entityManager.persist(cartItem);
        }
    }

    @Override
    public List<CartDTO> viewCart(String username) {
        String jpql = "SELECT c FROM CartDTO c WHERE c.username.username = :username";
        TypedQuery<CartDTO> query = entityManager.createQuery(jpql, CartDTO.class);
        query.setParameter("username", username);
        List<CartDTO> cartList = query.getResultList();

        // In ra console để kiểm tra

        return cartList;
    }

    @Override
    @Transactional
    public void editQuantity(String username, int bookID, int quantity) {
        String jpql = "FROM CartDTO WHERE username.username = :username AND bookID.bookID = :bookID";
        TypedQuery<CartDTO> query = entityManager.createQuery(jpql, CartDTO.class);
        query.setParameter("username", username);
        query.setParameter("bookID", bookID);

        if (!query.getResultList().isEmpty()) {
            CartDTO cartItem = query.getSingleResult();
            cartItem.setQuantity(quantity);
            entityManager.merge(cartItem);
        } else {
            throw new RuntimeException("Cart item not found with ID: " + bookID);
        }
    }

    @Override
    @Transactional
    public void deleteBookFromCart(String username, int bookID) {
        // Thay đổi câu truy vấn để xóa dựa trên bookID.bookID thay vì cartID
        String jpql = "DELETE FROM CartDTO c WHERE c.bookID.bookID = :bookID AND c.username.username = :username";
        int deletedCount = entityManager.createQuery(jpql)
                .setParameter("bookID", bookID)
                .setParameter("username", username)
                .executeUpdate();
        if (deletedCount > 0) {
            System.out.println("Cart item deleted successfully with bookID: " + bookID);
        } else {
            throw new RuntimeException("Cart item not found or username does not match.");
        }
    }
}
