package com.example.thuan.daos;

import java.util.List;

import com.example.thuan.models.BookDTO;

public interface BookDAO {
    void save(BookDTO bookDTO);

    BookDTO find(int bookID);

    void update(BookDTO bookDTO);

    void delete(int bookID);

    List<BookDTO> findAll();

    List<BookDTO> searchBooks(String searchTerm);

    List<BookDTO> findBooksByTitleAndAuthorAndPublisher(String bookTitle, String author, String publisher);

    List<BookDTO> sortBooks(String sortBy, String sortOrder);

    // List<BookDTO> filterBooksByCategory(int categoryID);

    public List<BookDTO> getBooksWithConditions(int offset,
            int pageSize,
            String bookTitle,
            String author,
            String translator,
            Integer publicationYear,
            String isbn,
            Integer bookStatus,
            String sort);

    public long countBooksWithConditions(String bookTitle,
            String author,
            String translator,
            Integer publicationYear,
            String isbn,
            Integer bookStatus);
}