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

    public AccountDTO registerAccount(String account, HttpServletResponse response);

    boolean verifyEmail(String token, String otpCodeRequest);

    public boolean resendOTP(String email);

    public AccountDTO findByEmail(String email);

    public List<AccountDTO> getAccounts(int offset, int pageSize);
}
