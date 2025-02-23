package com.example.thuan.daos;

import com.example.thuan.ultis.Status;
import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.exceptions.AppException;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.models.StaffDTO;
import com.example.thuan.ultis.EmailSenderUtil;
import com.example.thuan.ultis.ErrorCode;
import com.example.thuan.ultis.JwtUtil;
import com.example.thuan.ultis.RandomNumberGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletResponse;

@Repository
public class AccountDAOImpl implements AccountDAO {

    @Autowired
    EntityManager entityManager;

    @Autowired
    PasswordEncoder password = new BCryptPasswordEncoder(10);

    @Autowired
    EmailSenderUtil sender;

    @Autowired
    JwtUtil jwtUtil;

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
                // StaffDTO staff = query.getSingleResult();
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

    @Autowired
    private RandomNumberGenerator randomNumberGenerator;

    @Override
    @Transactional
    public AccountDTO registerAccount(String account, HttpServletResponse response) {
        try {
            ObjectMapper obj = new ObjectMapper();
            AccountDTO accountDTO = obj.readValue(account, AccountDTO.class);
            accountDTO.setAccStatus(Status.UNVERIFIED_STATUS.getValue());
            accountDTO.setRole(1);
            accountDTO.setPassword(password.encode(accountDTO.getPassword()));

            // Kiểm tra xem username hoặc email đã tồn tại
            String checkQuery = "SELECT COUNT(*) FROM AccountDTO a WHERE a.username = :username OR a.email = :email";
            Long count = entityManager.createQuery(checkQuery, Long.class)
                    .setParameter("username", accountDTO.getUsername())
                    .setParameter("email", accountDTO.getEmail())
                    .getSingleResult();

            if (count > 0) {
                throw new IllegalArgumentException("Username or Email already exists!");
            }

            // Tạo và lưu OTP vào DB
            String otp = randomNumberGenerator.generateNumber();
            accountDTO.setCode(otp);
            entityManager.persist(accountDTO);

            // Gửi email xác thực
            sender.sendEmail(accountDTO.getEmail(), otp);

            return accountDTO;
        } catch (IllegalArgumentException e) {
            throw e; // Ném lỗi để Controller bắt được (tránh lỗi 500)
        } catch (Exception e) {
            throw new RuntimeException("Đăng ký thất bại: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public boolean verifyEmail(String token, String otp) { // Nhận OTP từ request
        try {
            // Giải mã JWT để lấy email
            String email = jwtUtil.validateToken(token);

            // Kiểm tra OTP trong DB
            TypedQuery<AccountDTO> query = entityManager.createQuery(
                    "SELECT a FROM AccountDTO a WHERE a.email = :email", AccountDTO.class);
            query.setParameter("email", email);
            AccountDTO account = query.getSingleResult();

            if (account != null && account.getCode().equals(otp)) {
                account.setAccStatus(Status.ACTIVE_STATUS.getValue());
                account.setCode(null);
                entityManager.merge(account);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Email verification failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public AccountDTO findByEmail(String email) {
        TypedQuery<AccountDTO> query = entityManager.createQuery(
                "SELECT a FROM AccountDTO a WHERE a.email = :email", AccountDTO.class);
        query.setParameter("email", email);
        AccountDTO account = query.getSingleResult();
        return account;
    }

    @Transactional
    @Override
    public boolean resendOTP(String email) {
        try {
            // Tìm tài khoản theo email
            AccountDTO account = this.findByEmail(email);

            if (account == null) {
                throw new IllegalArgumentException("Tài khoản không tồn tại.");
            }

            if (account.getAccStatus() != Status.UNVERIFIED_STATUS.getValue()) {
                return false;
            }

            // Tạo OTP mới
            String newOtp = randomNumberGenerator.generateNumber();
            account.setCode(newOtp);
            entityManager.merge(account);

            // Gửi OTP qua email
            sender.sendEmail(account.getEmail(), newOtp);

            return true;
        } catch (Exception e) {
            System.out.println("Error in resend OTP: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<AccountDTO> getAccounts(int offset, int pageSize) {

        try {

            if (pageSize < 1) {
                throw new AppException(ErrorCode.PAGE_SIZE_NOT_VALID);
            }

            TypedQuery<AccountDTO> query = entityManager.createQuery("SELECT a FROM AccountDTO a", AccountDTO.class);
            query.setFirstResult(offset);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (AppException e) {
            throw new AppException(e.getErrorCode());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
