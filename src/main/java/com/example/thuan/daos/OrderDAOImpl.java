package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.OrderDTO;

import java.util.List;
import java.util.Map;

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

    @Override
    public List<OrderDTO> getOrders(int offset, int pageSize, String whereClause, String sort,
            Map<String, Object> parameters) {
        String baseQuery = "SELECT o FROM OrderDTO o";

        String orderClause = "";
        if (sort != null && !sort.trim().isEmpty()) {
            // Xử lý sắp xếp
            String sortField = sort;
            String sortOrder = "ASC";
            if (sort.startsWith("-")) {
                sortField = sort.substring(1);
                sortOrder = "DESC";
            }

            // Cho phép sắp xếp theo các trường phổ biến
            if (!("orderID".equals(sortField) || "orderCode".equals(sortField) ||
                    "customerName".equals(sortField) || "orderDate".equals(sortField) ||
                    "orderStatus".equals(sortField) || "totalAmount".equals(sortField))) {
                sortField = "orderID";
            }

            orderClause = " ORDER BY o." + sortField + " " + sortOrder;
        } else {
            // Mặc định sắp xếp theo orderID giảm dần (mới nhất trước)
            orderClause = " ORDER BY o.orderID DESC";
        }

        String jpql = baseQuery + whereClause + orderClause;
        TypedQuery<OrderDTO> query = entityManager.createQuery(jpql, OrderDTO.class);

        // Thiết lập các tham số từ Map
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }

        query.setFirstResult(offset);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    @Override
    public long countOrdersWithConditions(String whereClause, Map<String, Object> parameters) {
        String baseQuery = "SELECT COUNT(o) FROM OrderDTO o";
        String jpql = baseQuery + whereClause;
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);

        // Thiết lập các tham số từ Map
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }

        return query.getSingleResult();
    }

}
