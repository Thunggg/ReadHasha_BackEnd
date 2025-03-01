package com.example.thuan.daos;

import java.io.IOException;
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

        List<BookDTO> findBooksByTitleAndAuthorAndPublisher(String bookTitle, String author, String publisher);

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
                        String sort);

        long countBooksWithConditions(String bookTitle,
                        String author,
                        String translator,
                        Integer publicationYear,
                        String isbn,
                        Integer bookStatus);

        BookDTO processBookCreation(BookDTO book, MultipartFile image);

        String handleImageUpload(MultipartFile image, Integer bookId) throws IOException;

}