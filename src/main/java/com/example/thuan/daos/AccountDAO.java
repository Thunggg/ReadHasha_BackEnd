package com.example.thuan.daos;

import java.util.List;

import com.example.thuan.models.AccountDTO;

public interface AccountDAO {
    void save(AccountDTO accountDTO);

    AccountDTO findByUsername(String username);

    List<AccountDTO> searchAccounts(String username);

    void deleteByUsername(String username);

    List<AccountDTO> findAll();

    public AccountDTO registerAccount(String account);

    // public AccountDTO findDetailByUsernameAndStaff(String username, StaffDTO
    // staff);

    // public void addAccount(String account);

    // boolean verifyEmail(String otpCodeRequest);

    // boolean verifyAccount(String otpCodeRequest);

    // boolean changePassword(String passwordRequest);

    // void setPassword(String passwordRequest);

    // boolean sendEmail(String sendEmailRequest);
}
