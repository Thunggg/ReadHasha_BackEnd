package com.example.thuan.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.thuan.daos.AccountDAO;
import com.example.thuan.models.AccountDTO;

// @Slf4j // cho phép sử dụng log.infor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    final int ROLE_ADMIN = 0;
    final int ROLE_CUSTOMER = 1;
    final int ROLE_SELLER_STAFF = 2;
    final int ROLE_WAREHOUSE_STAFF = 3;
    final String DEFAULT_PASSWORD = "12345";
    AccountDAO accountDAO;
    // StaffDAO staffDAO;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    // @Autowired
    // public AccountController(AccountDAO accountDAO, StaffDAO staffDAO) {
    // this.accountDAO = accountDAO;
    // this.staffDAO = staffDAO;
    // }

    @Autowired
    public AccountController(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/")
    public List<AccountDTO> getAccounts() {
        List<AccountDTO> list = new ArrayList<>();
        for (AccountDTO accountDTO : accountDAO.findAll()) {
            if (accountDTO.getAccStatus() != null && accountDTO.getAccStatus() > 0) {
                list.add(accountDTO);
            }
        }
        return list;
    }

}
