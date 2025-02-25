package com.example.thuan.daos;

import java.sql.Date;
import java.util.List;

import com.example.thuan.models.AccountDTO;
import com.example.thuan.request.UpdateUserRequest;

import jakarta.servlet.http.HttpServletResponse;

public interface AccountDAO {
    void save(AccountDTO accountDTO);

    AccountDTO findByUsername(String username);

    List<AccountDTO> searchAccounts(String username);

    // void deleteByUsername(String username);

    public boolean deleteUserByUsername(String username);

    List<AccountDTO> findAll();

    public AccountDTO registerAccount(String account, HttpServletResponse response);

    boolean verifyEmail(String token, String otpCodeRequest);

    public boolean resendOTP(String email);

    public AccountDTO findByEmail(String email);

    public List<AccountDTO> getAccounts(int offset, int pageSize, String email, String userName, Date startDob,
            Date endDob, String sort);

    public List<AccountDTO> countAccountsWithConditions(String email, String userName, Date startDob, Date endDob);

    AccountDTO updateUser(UpdateUserRequest updateRequest);
}
