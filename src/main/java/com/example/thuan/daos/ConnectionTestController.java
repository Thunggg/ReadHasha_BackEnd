package com.example.thuan.daos;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class ConnectionTestController {

    private final DataSource dataSource;

    public ConnectionTestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/test-connection")
    public String testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return "Connection to SQL Server is successful!";
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }
}
