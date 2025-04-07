package com.example.thuan.daos;

import com.example.thuan.ultis.Status;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.thuan.exceptions.AppException;
import com.example.thuan.models.AccountDTO;
import com.example.thuan.request.ChangePasswordRequest;
import com.example.thuan.request.UpdateUserRequest;
import com.example.thuan.ultis.EmailSenderUtil;
import com.example.thuan.ultis.ErrorCode;
import com.example.thuan.ultis.JwtUtil;
import com.example.thuan.ultis.RandomNumberGenerator;
import com.example.thuan.ultis.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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

    // @Override
    // public AccountDTO findByUsername(String username) {
    // try {
    // Query query = entityManager.createQuery(
    // "Select a.username, a.firstName, a.lastName, a.dob, a.address, a.email,
    // a.role, a.sex, a.phone, a.accStatus, a.password, a.code From AccountDTO a
    // WHERE a.username=:username");
    // query.setParameter("username", username);
    // Object[] result = (Object[]) query.getSingleResult();
    // AccountDTO account = new AccountDTO();
    // account.setUsername((String) result[0]);
    // account.setFirstName((String) result[1]);
    // account.setLastName((String) result[2]);
    // account.setDob((Date) result[3]);
    // account.setAddress((String) result[4]);
    // account.setEmail((String) result[5]);
    // account.setRole((Integer) result[6]);
    // account.setSex((Integer) result[7]);
    // account.setPhone((String) result[8]);
    // account.setAccStatus((Integer) result[9]);
    // account.setPassword((String) result[10]);
    // account.setCode((String) result[11]);
    // return account;
    // } catch (Exception e) {
    // System.out.println("No found account with username = " + username);
    // System.out.println(e.getMessage());
    // }
    // return null;
    // }

    @Override
    public AccountDTO findByUsername(String username) {
        try {
            // Sử dụng JPQL để lấy thông tin tài khoản và giỏ hàng
            String jpql = "SELECT DISTINCT a FROM AccountDTO a " +
                    "LEFT JOIN FETCH a.cartCollection c " +
                    "LEFT JOIN FETCH c.bookID " +
                    "WHERE a.username = :username";

            TypedQuery<AccountDTO> query = entityManager.createQuery(jpql, AccountDTO.class);
            query.setParameter("username", username);

            AccountDTO account = query.getSingleResult();

            // Đảm bảo cartCollection được khởi tạo
            if (account.getCartCollection() != null) {
                account.getCartCollection().size(); // Force initialization
            }

            return account;
        } catch (NoResultException e) {
            System.out.println("No found account with username = " + username);
            return null;
        } catch (Exception e) {
            System.out.println("Error in findByUsername: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // @Override
    // @Transactional
    // public void deleteByUsername(String username) {
    // AccountDTO account = this.findByUsername(username);
    // if (account.getRole() == 1) {
    // account.setAccStatus(0);
    // entityManager.merge(account);
    // } else {
    // try {
    // TypedQuery<StaffDTO> query = entityManager.createQuery(
    // "SELECT s FROM StaffDTO s WHERE s.username.username = :username",
    // StaffDTO.class);
    // query.setParameter("username", username);
    // // StaffDTO staff = query.getSingleResult();
    // account.setAccStatus(0);
    // entityManager.merge(account);
    // } catch (Exception e) {
    // System.out.println("Error in findStaff: " + e.getMessage());
    // }
    // }

    // }

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

            String checkQuery1 = "SELECT COUNT(*) FROM AccountDTO a WHERE a.username = :username";
            Long count1 = entityManager.createQuery(checkQuery1, Long.class)
                    .setParameter("username", accountDTO.getUsername())
                    .getSingleResult();

            String checkQuery2 = "SELECT COUNT(*) FROM AccountDTO a WHERE a.email = :email";
            Long count2 = entityManager.createQuery(checkQuery2, Long.class)
                    .setParameter("email", accountDTO.getEmail())
                    .getSingleResult();

            if (count1 != 0 && count2 != 0) {
                throw new IllegalArgumentException("Username and Email already exists!");
            }

            if (count1 > 0 && count2 == 0) {
                throw new IllegalArgumentException("Username already exists!");
            }

            if (count1 == 0 && count2 > 0) {
                throw new IllegalArgumentException("Email already exists!");
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
        try {
            TypedQuery<AccountDTO> query = entityManager.createQuery(
                    "SELECT a FROM AccountDTO a WHERE a.email = :email",
                    AccountDTO.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // Trả về null nếu không tìm thấy
        }
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

            // if (account.getAccStatus() != Status.UNVERIFIED_STATUS.getValue()) {
            // return false;
            // }

            if (account.getAccStatus() == Status.INACTIVE_STATUS.getValue()) {
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
    public List<AccountDTO> getAccounts(int offset, int pageSize, String email, String userName, Date startDob,
            Date endDob, String sort, Integer accStatus) {

        try {

            // Xây dựng câu query động
            StringBuilder jpql = new StringBuilder("SELECT a FROM AccountDTO a WHERE 1=1 ");
            Map<String, Object> params = new HashMap<>();

            // Thêm điều kiện email
            if (email != null && !email.isEmpty()) {
                jpql.append("AND LOWER(a.email) LIKE LOWER(:email) ");
                params.put("email", "%" + email + "%");
            }

            // Thêm điều kiện userName
            if (userName != null && !userName.isEmpty()) {
                jpql.append("AND LOWER(a.username) LIKE LOWER(:userName) ");
                params.put("userName", "%" + userName + "%");
            }

            // Thêm điều kiện khoảng thời gian dob
            if (startDob != null) {
                jpql.append("AND a.dob >= :startDob ");
                params.put("startDob", startDob);
            }
            if (endDob != null) {
                jpql.append("AND a.dob <= :endDob ");
                params.put("endDob", endDob);
            }

            // Thêm điều kiện accStatus
            if (accStatus != null) {
                jpql.append("AND a.accStatus = :accStatus ");
                params.put("accStatus", accStatus);
            }

            // Xử lý sắp xếp
            if (sort != null) {
                if (sort.equalsIgnoreCase("-dob")) {
                    jpql.append("ORDER BY a.dob DESC");
                } else if (sort.equalsIgnoreCase("dob")) {
                    jpql.append("ORDER BY a.dob ASC");
                }
            } else {
                // Mặc định sắp xếp theo dob giảm dần nếu không có sort
                jpql.append("ORDER BY a.dob DESC");
            }

            // Tạo query
            TypedQuery<AccountDTO> query = entityManager.createQuery(jpql.toString(), AccountDTO.class);

            // Set parameters
            params.forEach(query::setParameter);

            // Phân trang
            query.setFirstResult(offset);
            query.setMaxResults(pageSize);

            return query.getResultList();
        } catch (AppException e) {
            throw new AppException(e.getErrorCode());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public List<AccountDTO> countAccountsWithConditions(String email, String userName, Date startDob, Date endDob,
            Integer accStatus) {

        try {

            // Xây dựng câu query động
            StringBuilder jpql = new StringBuilder("SELECT a FROM AccountDTO a WHERE 1=1 ");
            Map<String, Object> params = new HashMap<>();

            // Thêm điều kiện email
            if (email != null && !email.isEmpty()) {
                jpql.append("AND LOWER(a.email) LIKE LOWER(:email) ");
                params.put("email", "%" + email + "%");
            }

            // Thêm điều kiện userName
            if (userName != null && !userName.isEmpty()) {
                jpql.append("AND LOWER(a.username) LIKE LOWER(:userName) ");
                params.put("userName", "%" + userName + "%");
            }

            // Thêm điều kiện khoảng thời gian dob
            if (startDob != null) {
                jpql.append("AND a.dob >= :startDob ");
                params.put("startDob", startDob);
            }
            if (endDob != null) {
                jpql.append("AND a.dob <= :endDob ");
                params.put("endDob", endDob);
            }

            // Thêm điều kiện accStatus
            if (accStatus != null) {
                jpql.append("AND a.accStatus = :accStatus ");
                params.put("accStatus", accStatus);
            }

            // Tạo query
            TypedQuery<AccountDTO> query = entityManager.createQuery(jpql.toString(), AccountDTO.class);

            // Set parameters
            params.forEach(query::setParameter);

            return query.getResultList();
        } catch (AppException e) {
            throw new AppException(e.getErrorCode());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // @Transactional
    // @Override
    // public boolean deleteUserByUsername(String username) {
    // try {
    // AccountDTO account = findByUsername(username);
    // if (account == null) {
    // return false;
    // }
    // if (account.getRole() == Role.ROLE_ADMIN.getValue()) {
    // return false;
    // }

    // entityManager.createQuery("DELETE FROM AccountDTO a WHERE a.username =
    // :username")
    // .setParameter("username", username)
    // .executeUpdate();

    // return true;
    // } catch (Exception e) {
    // throw new AppException(ErrorCode.INTERNAL_ERROR);
    // }
    // }

    @Transactional
    @Override
    public boolean deleteUserByUsername(String username) {
        try {
            AccountDTO account = findByUsername(username);
            if (account == null) {
                return false;
            }
            if (account.getRole() == Role.ROLE_ADMIN.getValue()) {
                return false;
            }

            // Đặt trạng thái tài khoản thành không hoạt động (thay vì xóa)
            account.setAccStatus(Status.INACTIVE_STATUS.getValue());
            entityManager.merge(account);

            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    @Override
    public AccountDTO updateUser(UpdateUserRequest updateRequest) {
        AccountDTO account = findByUsername(updateRequest.getUsername());

        if (account == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if (account.getRole() == Role.ROLE_ADMIN.getValue()) {
            throw new AppException(ErrorCode.USER_NOT_UPDATE);
        }

        // 2. Cập nhật các trường được phép thay đổi
        if (updateRequest.getFirstName() != null) {
            account.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            account.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getDob() != null) {
            account.setDob(updateRequest.getDob());
        }
        if (updateRequest.getPhone() != null) {
            account.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getAddress() != null) {
            account.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getSex() != null) {
            account.setSex(updateRequest.getSex());
        }
        if (updateRequest.getAccStatus() != null) {
            account.setAccStatus(updateRequest.getAccStatus());
        }

        // 3. Lưu thay đổi
        AccountDTO managedAccount = entityManager.merge(account);
        entityManager.flush();
        entityManager.refresh(managedAccount);

        return managedAccount;
    }

    @Override
    @Transactional
    public boolean changePassword(String username, ChangePasswordRequest request) {
        // 1. Tìm tài khoản
        AccountDTO account = findByUsername(username);
        if (account == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. Kiểm tra mật khẩu cũ
        if (!password.matches(request.getOldPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        // 3. Mã hóa và cập nhật mật khẩu mới
        account.setPassword(password.encode(request.getNewPassword()));
        entityManager.merge(account);

        return true;
    }

    // AccountDAOImpl.java
    @Override
    @Transactional
    public boolean sendVerificationOTP(String email) {
        try {
            AccountDTO account = findByEmail(email);

            // Kiểm tra email có tồn tại không
            if (account == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            // Kiểm tra trạng thái tài khoản (gửi otp nếu tài khoản ko bị khóa và đã xác
            // thực)
            if (account.getAccStatus() == Status.UNVERIFIED_STATUS.getValue() ||
                    account.getAccStatus() == Status.INACTIVE_STATUS.getValue()) {
                throw new AppException(ErrorCode.USER_UNVERIFIED_STATUS_OR_INACTIVE_STATUS);
            }

            // Tạo OTP mới
            String newOtp = randomNumberGenerator.generateNumber();
            account.setCode(newOtp);
            entityManager.merge(account);

            // Gửi email
            sender.sendEmail(email, newOtp);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    @Transactional
    public boolean resetPassword(String email, String newPassword) {
        try {
            AccountDTO account = findByEmail(email);
            if (account == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            // Kiểm tra trạng thái tài khoản
            if (account.getAccStatus() != Status.ACTIVE_STATUS.getValue()) {
                throw new AppException(ErrorCode.USER_UNVERIFIED_STATUS_OR_INACTIVE_STATUS);
            }

            // Cập nhật mật khẩu mới
            account.setPassword(password.encode(newPassword));
            account.setCode(null); // Clear OTP sau khi reset thành công
            entityManager.merge(account);
            return true;

        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

}
