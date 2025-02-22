package com.example.thuan.controllers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.thuan.daos.AuthenticationDAO;
import com.example.thuan.exceptions.AuthenticationException;
import com.example.thuan.request.AuthenticationRequest;
import com.example.thuan.request.IntrospectRequest;
import com.example.thuan.request.RefreshToken;
import com.example.thuan.respone.AuthenticationResponse;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.respone.IntrospectResponse;
import com.example.thuan.ultis.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    @Autowired
    AuthenticationDAO authenticationDAO;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping(value = "/login")
    public ResponseEntity<BaseResponse<AuthenticationResponse>> authenticate(@RequestPart("login") String login,
            HttpServletResponse response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AuthenticationRequest request = objectMapper.readValue(login, AuthenticationRequest.class);
            BaseResponse<AuthenticationResponse> res = authenticationDAO.authenticate(request, response);
            return ResponseEntity.status(res.getStatusCode()).body(res);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    BaseResponse.error("Dữ liệu không hợp lệ", 400, null));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(ex.getStatus()).body(
                    BaseResponse.error(ex.getMessage(), ex.getErrorCode(), null));
        }
    }

    // @PostMapping("/introspect")
    // ResponseEntity<IntrospectResponse> authenticate(@RequestBody
    // IntrospectRequest request) {
    // IntrospectResponse response = authenticationDAO.introspect(request);
    // return ResponseEntity.status(HttpStatus.OK).body(response);
    // }

    // @PostMapping("/logout")
    // void logout(@RequestBody LogoutRequest request) throws Exception {
    // System.out.println("logout successfully!");
    // authenticationDAO.logout(request);
    // }

    @PostMapping("/refresh")
    ResponseEntity<AuthenticationResponse> authenticate(@RequestBody RefreshToken request) throws Exception {
        AuthenticationResponse response = jwtUtil.refreshToken(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}