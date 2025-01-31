package com.example.thuan.daos;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.models.AccountDTO;
import com.example.thuan.models.StaffDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

@Repository
public class AccountDAOImpl implements AccountDAO {

    final int ACTIVE_STATUS = 1;
    final int INACTIVE_STATUS = 0;
    final int UNVERIFIED_STATUS = 2;
    final int UNVERIFIED_ADMIN_CREATED_STATUS = 4;
    final String DEFAULT_PASSWORD = "12345";
    // define entity manager
    EntityManager entityManager;
    PasswordEncoder password = new BCryptPasswordEncoder(10);

    // @Autowired
    // EmailSenderUtil sender;

    @Autowired
    public AccountDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // implements method
    @Override
    @Transactional
    public void save(AccountDTO accountDTO) {
        AccountDTO account = this.findByUsername(accountDTO.getUsername());
        if (account != null) {
            entityManager.merge(accountDTO);
        } else {
            entityManager.persist(accountDTO);
        }
    }

    @Override
    public List<AccountDTO> searchAccounts(String searchKey) {
        String str = "FROM AccountDTO WHERE LOWER(username) LIKE :searchKey OR LOWER(firstName) LIKE :searchKey OR LOWER(lastName) LIKE :searchKey";
        TypedQuery<AccountDTO> query = entityManager.createQuery(str, AccountDTO.class);
        query.setParameter("searchKey", "%" + searchKey.toLowerCase() + "%");
        return query.getResultList();
    }

    @Override
    public AccountDTO findByUsername(String username) {
        try {
            Query query = entityManager.createQuery(
                    "Select a.username, a.firstName, a.lastName, a.dob, a.address, a.email, a.role, a.sex, a.phone, a.accStatus, a.password, a.code From AccountDTO a WHERE a.username=:username");
            query.setParameter("username", username);
            Object[] result = (Object[]) query.getSingleResult();
            AccountDTO account = new AccountDTO();
            account.setUsername((String) result[0]);
            account.setFirstName((String) result[1]);
            account.setLastName((String) result[2]);
            account.setDob((Date) result[3]);
            account.setAddress((String) result[4]);
            account.setEmail((String) result[5]);
            account.setRole((Integer) result[6]);
            account.setSex((Integer) result[7]);
            account.setPhone((String) result[8]);
            account.setAccStatus((Integer) result[9]);
            account.setPassword((String) result[10]);
            account.setCode((String) result[11]);
            return account;
        } catch (Exception e) {
            System.out.println("No found account with username = " + username);
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        AccountDTO account = this.findByUsername(username);
        if (account.getRole() == 1) {
            account.setAccStatus(0);
            entityManager.merge(account);
        } else {
            try {
                TypedQuery<StaffDTO> query = entityManager.createQuery(
                        "SELECT s FROM StaffDTO s WHERE s.username.username = :username", StaffDTO.class);
                query.setParameter("username", username);
                StaffDTO staff = query.getSingleResult();
                account.setAccStatus(0);
                entityManager.merge(account);
            } catch (Exception e) {
                System.out.println("Error in findStaff: " + e.getMessage());
            }
        }

    }

    @Override
    public List<AccountDTO> findAll() {
        TypedQuery<AccountDTO> query = entityManager.createQuery("From AccountDTO a WHERE a.accStatus>0",
                AccountDTO.class);
        return query.getResultList();
    }
}
