package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.InvalidatedTokenDTO;

@Repository
public class InvalidatedTokenDAO {
    @Autowired
    EntityManager entityManager;

    @Transactional
    public void save(InvalidatedTokenDTO token) {
        String jpql = "INSERT INTO InvalidatedTokenDTO (ITID, expiryTime) "
                + "VALUES ( :id, :expiryTime)";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("id", token.getITID());
        query.setParameter("expiryTime", token.getExpiryTime());

        query.executeUpdate();
    }

    public boolean existsById(String jwtid) {
        String jpql = "FROM InvalidatedTokenDTO WHERE id=:id";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("id", jwtid);
        List result = query.getResultList();
        return result.size() > 0;
    }

    @Transactional
    public void deleteByITID(String jwtid) {
        String jpql = "DELETE FROM InvalidatedTokenDTO WHERE ITID=:id";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("id", jwtid);
        query.executeUpdate();
    }
}
