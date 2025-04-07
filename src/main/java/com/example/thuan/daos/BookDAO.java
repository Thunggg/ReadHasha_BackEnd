package com.example.thuan.daos;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.thuan.models.BookDTO;

public interface BookDAO {
        BookDTO save(BookDTO bookDTO);

        BookDTO find(int bookID);

        BookDTO update(BookDTO book);

        void delete(int bookID);

        List<BookDTO> findAll();

        List<BookDTO> searchBooks(String searchTerm);

        List<BookDTO> findBooksByTitleAndAuthorAndPublisher(String bookTitle, String author, String publisher,
                        Integer excludeBookId);

        List<BookDTO> sortBooks(String sortBy, String sortOrder);

        // List<BookDTO> filterBooksByCategory(int categoryID);

        List<BookDTO> getBooksWithConditions(int offset,
                        int pageSize,
                        String bookTitle,
                        String author,
                        String translator,
                        Integer publicationYear,
                        String isbn,
                        Integer bookStatus,
                        List<Integer> categoryIds,
                        String sort,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        String mainText,
                        boolean homePage);

        long countBooksWithConditions(String bookTitle,
                        String author,
                        String translator,
                        Integer publicationYear,
                        String isbn,
                        Integer bookStatus,
                        List<Integer> categoryIds,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        String mainText);

        BookDTO processBookCreation(BookDTO book, MultipartFile image);

        String handleImageUpload(MultipartFile image, Integer bookId);

        public void deleteBookWithCategories(Integer bookId);

        BookDTO processBookUpdate(BookDTO book, MultipartFile image);

        public List<BookDTO> findBooksByCategory(int catID);

        public List<BookDTO> findBooksByCategoryIds(List<Integer> categoryIds);
}