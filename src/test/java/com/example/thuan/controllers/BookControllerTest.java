package com.example.thuan.controllers;

import com.example.thuan.daos.BookDAO;
import com.example.thuan.models.BookDTO;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.ultis.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class BookControllerTest {

        private MockMvc mockMvc;

        @Mock
        private BookDAO bookDAO;

        @Mock
        private ObjectMapper objectMapper;

        @InjectMocks
        private BookController bookController;

        private BookDTO testBook;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();

                // Tạo một đối tượng BookDTO để sử dụng trong các test
                testBook = new BookDTO();
                testBook.setBookID(1005);
                testBook.setBookTitle("Giải thích ngữ pháp Mai Lan Hương");
                testBook.setAuthor("Mai Lan Hương");
                testBook.setPublisher("NXB Kim Đồng");
                testBook.setPublicationYear(2023);
                testBook.setIsbn("3123213321333");
                testBook.setBookDescription("Giải thích ngữ pháp");
                testBook.setHardcover(321);
                testBook.setDimension("20 x 32");
                testBook.setWeight(333.0);
                testBook.setBookPrice(new BigDecimal("320000.00"));
                testBook.setBookQuantity(5);
                testBook.setBookStatus(1);
        }

        @Test
        @DisplayName("Test 1: Thêm sách thành công với đầy đủ thông tin và hình ảnh")
        void testAddBookSuccessWithImage() throws Exception {
                // Chuẩn bị dữ liệu
                String bookJson = "{\"bookTitle\":\"Giải thích ngữ pháp Mai Lan Hương\",\"author\":\"Mai Lan Hương\"}";
                MockMultipartFile bookFile = new MockMultipartFile(
                                "book", "", "application/json", bookJson.getBytes());

                MockMultipartFile imageFile = new MockMultipartFile(
                                "image", "test-image.jpg", "image/jpeg", "test image content".getBytes());

                // Mock các phương thức
                when(objectMapper.readValue(bookJson, BookDTO.class)).thenReturn(testBook);
                when(bookDAO.processBookCreation(any(BookDTO.class), any(MultipartFile.class))).thenReturn(testBook);

                // Thực hiện request và kiểm tra kết quả
                mockMvc.perform(multipart("/api/v1/books/add-book")
                                .file(bookFile)
                                .file(imageFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(200))
                                .andExpect(jsonPath("$.message").value("Tạo mới sách thành công!"));

                // Verify rằng phương thức processBookCreation đã được gọi
                verify(bookDAO, times(1)).processBookCreation(any(BookDTO.class), any(MultipartFile.class));
        }

        @Test
        @DisplayName("Test 2: Thêm sách thành công không có hình ảnh")
        void testAddBookSuccessWithoutImage() throws Exception {
                // Chuẩn bị dữ liệu
                String bookJson = "{\"bookTitle\":\"Giải thích ngữ pháp Mai Lan Hương\",\"author\":\"Mai Lan Hương\"}";
                MockMultipartFile bookFile = new MockMultipartFile(
                                "book", "", "application/json", bookJson.getBytes());

                // Mock các phương thức
                when(objectMapper.readValue(bookJson, BookDTO.class)).thenReturn(testBook);
                when(bookDAO.processBookCreation(any(BookDTO.class), isNull())).thenReturn(testBook);

                // Thực hiện request và kiểm tra kết quả
                mockMvc.perform(multipart("/api/v1/books/add-book")
                                .file(bookFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(200))
                                .andExpect(jsonPath("$.message").value("Tạo mới sách thành công!"));

                // Verify rằng phương thức processBookCreation đã được gọi
                verify(bookDAO, times(1)).processBookCreation(any(BookDTO.class), isNull());
        }

        @Test
        @DisplayName("Test 3: Thêm sách thất bại do thiếu thông tin bắt buộc")
        void testAddBookFailureMissingRequiredFields() throws Exception {
                // Chuẩn bị dữ liệu
                String bookJson = "{\"author\":\"Mai Lan Hương\"}"; // Thiếu bookTitle
                MockMultipartFile bookFile = new MockMultipartFile(
                                "book", "", "application/json", bookJson.getBytes());

                // Tạo một BookDTO thiếu thông tin
                BookDTO invalidBook = new BookDTO();
                invalidBook.setAuthor("Mai Lan Hương");
                // Thiếu bookTitle

                // Mock các phương thức
                when(objectMapper.readValue(bookJson, BookDTO.class)).thenReturn(invalidBook);
                when(bookDAO.processBookCreation(any(BookDTO.class), any(MultipartFile.class)))
                                .thenThrow(new IllegalArgumentException("Tiêu đề sách không được để trống"));

                // Thực hiện request và kiểm tra kết quả
                mockMvc.perform(multipart("/api/v1/books/add-book")
                                .file(bookFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(400))
                                .andExpect(jsonPath("$.message").value("Tiêu đề sách không được để trống"));
        }

        @Test
        @DisplayName("Test 4: Thêm sách thất bại do định dạng hình ảnh không hợp lệ")
        void testAddBookFailureInvalidImageFormat() throws Exception {
                // Chuẩn bị dữ liệu
                String bookJson = "{\"bookTitle\":\"Giải thích ngữ pháp Mai Lan Hương\",\"author\":\"Mai Lan Hương\"}";
                MockMultipartFile bookFile = new MockMultipartFile(
                                "book", "", "application/json", bookJson.getBytes());

                MockMultipartFile imageFile = new MockMultipartFile(
                                "image", "test-image.txt", "text/plain", "this is not an image".getBytes());

                // Mock các phương thức
                when(objectMapper.readValue(bookJson, BookDTO.class)).thenReturn(testBook);
                when(bookDAO.processBookCreation(any(BookDTO.class), any(MultipartFile.class)))
                                .thenThrow(new RuntimeException("Định dạng hình ảnh không hợp lệ"));

                // Thực hiện request và kiểm tra kết quả
                mockMvc.perform(multipart("/api/v1/books/add-book")
                                .file(bookFile)
                                .file(imageFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(500))
                                .andExpect(jsonPath("$.message").value("Lỗi hệ thống"));
        }

        @Test
        @DisplayName("Test 5: Thêm sách thất bại do lỗi hệ thống")
        void testAddBookFailureSystemError() throws Exception {
                // Chuẩn bị dữ liệu
                String bookJson = "{\"bookTitle\":\"Giải thích ngữ pháp Mai Lan Hương\",\"author\":\"Mai Lan Hương\"}";
                MockMultipartFile bookFile = new MockMultipartFile(
                                "book", "", "application/json", bookJson.getBytes());

                // Mock các phương thức
                when(objectMapper.readValue(bookJson, BookDTO.class)).thenReturn(testBook);
                when(bookDAO.processBookCreation(any(BookDTO.class), any(MultipartFile.class)))
                                .thenThrow(new RuntimeException("Database connection error"));

                // Thực hiện request và kiểm tra kết quả
                mockMvc.perform(multipart("/api/v1/books/add-book")
                                .file(bookFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(500))
                                .andExpect(jsonPath("$.message").value("Lỗi hệ thống"));
        }
}