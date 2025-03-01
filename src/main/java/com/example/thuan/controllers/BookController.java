package com.example.thuan.controllers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.thuan.daos.BookDAO;
import com.example.thuan.exceptions.AppException;
import com.example.thuan.models.BookDTO;
import com.example.thuan.respone.BaseResponse;
import com.example.thuan.respone.Meta;
import com.example.thuan.respone.PaginationResponse;
import com.example.thuan.ultis.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("api/v1/books")
@CrossOrigin(origins = "http://localhost:5173")
public class BookController {
    private final BookDAO bookDAO;
    private final ObjectMapper objectMapper;

    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public BookController(BookDAO bookDAO, ObjectMapper objectMapper) {
        this.bookDAO = bookDAO;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    @Transactional
    public List<BookDTO> getBooksList() {
        List<BookDTO> books = bookDAO.findAll();
        // Khởi tạo `bookCategories` và các `CategoryDTO` liên quan
        books.forEach(book -> {
            Hibernate.initialize(book.getBookCategories());
            book.getBookCategories().forEach(bookCategory -> {
                Hibernate.initialize(bookCategory.getCatId());
            });
        });
        return books;
    }

    @GetMapping("/book-pagination")
    @Transactional
    public BaseResponse<PaginationResponse<BookDTO>> getBooksPaginated(
            @RequestParam(name = "bookTitle", required = false) String bookTitle,
            @RequestParam(name = "author", required = false) String author,
            @RequestParam(name = "translator", required = false) String translator,
            @RequestParam(name = "publicationYear", required = false) Integer publicationYear,
            @RequestParam(name = "isbn", required = false) String isbn,
            @RequestParam(name = "bookStatus", required = false) Integer bookStatus,
            @RequestParam(name = "current", defaultValue = "1") int current,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sort", required = false) String sort) {

        try {
            // Xử lý sắp xếp theo Price và Quantity
            String orderBy = "b.bookID DESC"; // Mặc định sắp xếp theo bookID giảm dần
            if (sort != null) {
                switch (sort) {
                    case "bookPrice":
                        orderBy = "b.bookPrice ASC";
                        break;
                    case "-bookPrice":
                        orderBy = "b.bookPrice DESC";
                        break;
                    case "bookQuantity":
                        orderBy = "b.bookQuantity ASC";
                        break;
                    case "-bookQuantity":
                        orderBy = "b.bookQuantity DESC";
                        break;
                }
            }

            int offset = (current - 1) * pageSize;

            // Gọi DAO với các tham số tìm kiếm
            List<BookDTO> data = bookDAO.getBooksWithConditions(offset, pageSize, bookTitle, author, translator,
                    publicationYear, isbn, bookStatus, orderBy);

            // Khởi tạo các quan hệ lazy loading
            data.forEach(book -> {
                Hibernate.initialize(book.getBookCategories());
                book.getBookCategories().forEach(bookCategory -> {
                    Hibernate.initialize(bookCategory.getCatId());
                });
            });

            // Đếm tổng số bản ghi theo điều kiện
            long total = bookDAO.countBooksWithConditions(bookTitle, author, translator, publicationYear, isbn,
                    bookStatus);

            int pages = (pageSize == 0) ? 0 : (int) Math.ceil((double) total / pageSize);

            Meta meta = new Meta();
            meta.setCurrent(current);
            meta.setPageSize(pageSize);
            meta.setPages(pages);
            meta.setTotal(total);

            PaginationResponse<BookDTO> pagingRes = new PaginationResponse<>(data, meta);
            return BaseResponse.success("Lấy danh sách sách thành công!", 200, pagingRes, null, null);
        } catch (AppException e) {
            return BaseResponse.error(e.getMessage(), e.getErrorCode().getCode(), null);
        }
    }

    @PostMapping(path = "/add-book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<BookDTO> addBook(
            @RequestPart("book") String bookJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            BookDTO book = objectMapper.readValue(bookJson, BookDTO.class);
            BookDTO savedBook = bookDAO.processBookCreation(book, image);
            return BaseResponse.success("Tạo mới sách thành công!", 200, savedBook, null, null);

        } catch (AppException e) {
            return BaseResponse.error(
                    e.getMessage(),
                    e.getErrorCode().getCode(),
                    null);
        } catch (Exception e) {
            return BaseResponse.error(
                    ErrorCode.INTERNAL_ERROR.getMessage(),
                    ErrorCode.INTERNAL_ERROR.getCode(),
                    null);
        }
    }
}
