package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.OrderDTO;

import java.util.List;

@Repository
public class OrderDAOImpl implements OrderDAO {

    EntityManager entityManager;

    @Autowired
    public OrderDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(OrderDTO orderDTO) {
        entityManager.persist(orderDTO);
    }

    @Override
    public OrderDTO find(int orderID) {
        return entityManager.find(OrderDTO.class, orderID);
    }

    @Override
    @Transactional
    public void update(OrderDTO orderDTO) {
        entityManager.merge(orderDTO);
    }

    @Override
    public void delete(int orderID) {
        entityManager.remove(this.find(orderID));
    }

    @Override
    public List<OrderDTO> findAll() {
        TypedQuery<OrderDTO> query = entityManager.createQuery("SELECT o FROM OrderDTO o JOIN FETCH o.username",
                OrderDTO.class);
        return query.getResultList();
    }

    @Override
    public List<OrderDTO> searchByOrderId(int orderID) {
        TypedQuery<OrderDTO> query = entityManager.createQuery(
                "SELECT o FROM OrderDTO o JOIN FETCH o.username WHERE o.orderID = :orderID", OrderDTO.class);
        query.setParameter("orderID", orderID);
        return query.getResultList();
    }

    @Override
    public List<OrderDTO> findByUsername(String username) {
        TypedQuery<OrderDTO> query = entityManager.createQuery(
                "SELECT DISTINCT o FROM OrderDTO o " +
                        "JOIN FETCH o.username " +
                        "LEFT JOIN FETCH o.orderDetailList od " +
                        "LEFT JOIN FETCH od.bookID " +
                        "WHERE o.username.username = :username",
                OrderDTO.class);
        query.setParameter("username", username);
        return query.getResultList();
    }
}
