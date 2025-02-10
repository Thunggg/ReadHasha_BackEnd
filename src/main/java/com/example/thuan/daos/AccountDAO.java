package com.example.thuan.daos;

import java.util.List;

import com.example.thuan.models.AccountDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface AccountDAO {
    void save(AccountDTO accountDTO);

    AccountDTO findByUsername(String username);

    List<AccountDTO> searchAccounts(String username);

    void deleteByUsername(String username);

    List<AccountDTO> findAll();

    // public AccountDTO registerAccount(String account);

    public AccountDTO registerAccount(String account, HttpServletResponse response);

    // public AccountDTO findDetailByUsernameAndStaff(String username, StaffDTO
    // staff);

    // public void addAccount(String account);

    boolean verifyEmail(String token, String otpCodeRequest);

    // boolean verifyAccount(String otpCodeRequest);

    // boolean changePassword(String passwordRequest);

    // void setPassword(String passwordRequest);

    // boolean sendEmail(String sendEmailRequest);
}
