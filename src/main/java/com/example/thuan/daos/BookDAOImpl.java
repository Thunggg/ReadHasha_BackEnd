package com.example.thuan.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.thuan.exceptions.AppException;
import com.example.thuan.models.BookCategoryDTO;
import com.example.thuan.models.BookDTO;
import com.example.thuan.ultis.ErrorCode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Repository
public class BookDAOImpl implements BookDAO {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
            "b.bookPrice ASC", "b.bookPrice DESC",
            "b.bookQuantity ASC", "b.bookQuantity DESC",
            "b.bookID DESC");

    EntityManager entityManager;

    @Autowired
    public BookDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public BookDTO save(BookDTO bookDTO) {
        if (bookDTO.getBookID() != null) {
            entityManager.merge(bookDTO);
        } else {
            entityManager.persist(bookDTO);
        }
        entityManager.flush(); // Ensure ID is generated
        return bookDTO;
    }

    @Override
    public BookDTO find(int bookId) {
        return entityManager.find(BookDTO.class, bookId);
    }

    @Override
    @Transactional
    public BookDTO update(BookDTO book) {
        BookDTO existingBook = find(book.getBookID());
        if (existingBook == null) {
            throw new IllegalArgumentException("Book with ID " + book.getBookID() + " not found");
        }
        return entityManager.merge(book);
    }

    @Override
    @Transactional
    public void delete(int bookID) {
        entityManager.remove(this.find(bookID));
    }

    @Override
    public List<BookDTO> findAll() {
        TypedQuery<BookDTO> query = entityManager.createQuery("From BookDTO", BookDTO.class);
        return query.getResultList();
    }

    @Override
    public List<BookDTO> sortBooks(String sortBy, String sortOrder) {
        String queryString = "FROM BookDTO b WHERE b.bookStatus = 1"; // Chỉ lấy sách hợp lệ
        if ("price".equalsIgnoreCase(sortBy)) {
            queryString += " ORDER BY b.bookPrice " + ("desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC");
        } else if ("title".equalsIgnoreCase(sortBy)) {
            queryString += " ORDER BY b.bookTitle " + ("desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC");
        }
        TypedQuery<BookDTO> query = entityManager.createQuery(queryString, BookDTO.class);
        return query.getResultList();
    }

    // @Override
    // public List<BookDTO> filterBooksByCategory(int categoryID) {
    // System.out.println("Category ID: " + categoryID);
    // CategoryDAOlmpl dao = new CategoryDAOlmpl(entityManager);
    // CategoryDTO category = dao.find(categoryID);
    // List<BookDTO> result = entityManager
    // .createQuery("SELECT b FROM BookDTO b JOIN FETCH b.bookCategories c WHERE
    // c.catId = :category",
    // BookDTO.class)
    // .setParameter("category", category)
    // .getResultList();
    // System.out.println(result.size());
    // return result;
    // }

    @Override
    public List<BookDTO> searchBooks(String searchTerm) {
        String jpql = "FROM BookDTO WHERE " +
                "LOWER(bookTitle) LIKE :searchTerm OR " +
                "LOWER(author) LIKE :searchTerm OR " +
                "LOWER(publisher) LIKE :searchTerm";
        TypedQuery<BookDTO> query = entityManager.createQuery(jpql, BookDTO.class);
        query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        return query.getResultList();
    }

    @Override
    public List<BookDTO> findBooksByTitleAndAuthorAndPublisher(String bookTitle, String author, String publisher) {
        System.out.println(
                "Searching for book with title: " + bookTitle + ", author: " + author + ", publisher: " + publisher);
        List<BookDTO> result = entityManager.createQuery(
                "SELECT b FROM BookDTO b WHERE b.bookTitle = :bookTitle AND b.author = :author AND b.publisher = :publisher",
                BookDTO.class)
                .setParameter("bookTitle", bookTitle)
                .setParameter("author", author)
                .setParameter("publisher", publisher)
                .getResultList();
        System.out.println("Found books: " + result.size());
        return result;
    }

    @Override
    public List<BookDTO> getBooksWithConditions(int offset,
            int pageSize,
            String bookTitle,
            String author,
            String translator,
            Integer publicationYear,
            String isbn,
            Integer bookStatus,
            String sort) {
        // Tạo câu truy vấn cơ bản
        String queryStr = "SELECT b FROM BookDTO b WHERE 1 = 1";

        // Thêm điều kiện tìm kiếm
        if (bookTitle != null && !bookTitle.isEmpty()) {
            queryStr += " AND LOWER(b.bookTitle) LIKE LOWER(:bookTitle)";
        }
        if (author != null && !author.isEmpty()) {
            queryStr += " AND LOWER(b.author) LIKE LOWER(:author)";
        }
        if (translator != null && !translator.isEmpty()) {
            queryStr += " AND LOWER(b.translator) LIKE LOWER(:translator)";
        }
        if (publicationYear != null) {
            queryStr += " AND b.publicationYear = :publicationYear";
        }
        if (isbn != null && !isbn.isEmpty()) {
            queryStr += " AND b.isbn = :isbn";
        }
        if (bookStatus != null) {
            queryStr += " AND b.bookStatus = :bookStatus";
        }

        // Sửa lại cách xử lý sort
        if (sort != null && !sort.isEmpty()) {
            if (!ALLOWED_SORT_FIELDS.contains(sort)) {
                sort = "b.bookID DESC";
            }
            queryStr += " ORDER BY " + sort;
        }
        // Tạo query
        Query query = entityManager.createQuery(queryStr, BookDTO.class);

        // Đặt tham số
        if (bookTitle != null && !bookTitle.isEmpty()) {
            query.setParameter("bookTitle", "%" + bookTitle + "%");
        }
        if (author != null && !author.isEmpty()) {
            query.setParameter("author", "%" + author + "%");
        }
        if (translator != null && !translator.isEmpty()) {
            query.setParameter("translator", "%" + translator + "%");
        }
        if (publicationYear != null) {
            query.setParameter("publicationYear", publicationYear);
        }
        if (isbn != null && !isbn.isEmpty()) {
            query.setParameter("isbn", isbn);
        }
        if (bookStatus != null) {
            query.setParameter("bookStatus", bookStatus);
        }

        // Phân trang
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        // Thực thi truy vấn
        return query.getResultList();
    }

    @Override
    public long countBooksWithConditions(String bookTitle,
            String author,
            String translator,
            Integer publicationYear,
            String isbn,
            Integer bookStatus) {
        // Tạo câu truy vấn đếm
        String queryStr = "SELECT COUNT(b) FROM BookDTO b WHERE 1 = 1";

        // Thêm điều kiện tìm kiếm
        if (bookTitle != null && !bookTitle.isEmpty()) {
            queryStr += " AND LOWER(b.bookTitle) LIKE LOWER(:bookTitle)";
        }
        if (author != null && !author.isEmpty()) {
            queryStr += " AND LOWER(b.author) LIKE LOWER(:author)";
        }
        if (translator != null && !translator.isEmpty()) {
            queryStr += " AND LOWER(b.translator) LIKE LOWER(:translator)";
        }
        if (publicationYear != null) {
            queryStr += " AND b.publicationYear = :publicationYear";
        }
        if (isbn != null && !isbn.isEmpty()) {
            queryStr += " AND b.isbn = :isbn";
        }
        if (bookStatus != null) {
            queryStr += " AND b.bookStatus = :bookStatus";
        }

        // Tạo query
        Query query = entityManager.createQuery(queryStr);

        // Đặt tham số
        if (bookTitle != null && !bookTitle.isEmpty()) {
            query.setParameter("bookTitle", "%" + bookTitle + "%");
        }
        if (author != null && !author.isEmpty()) {
            query.setParameter("author", "%" + author + "%");
        }
        if (translator != null && !translator.isEmpty()) {
            query.setParameter("translator", "%" + translator + "%");
        }
        if (publicationYear != null) {
            query.setParameter("publicationYear", publicationYear);
        }
        if (isbn != null && !isbn.isEmpty()) {
            query.setParameter("isbn", isbn);
        }
        if (bookStatus != null) {
            query.setParameter("bookStatus", bookStatus);
        }

        // Thực thi truy vấn và trả về kết quả
        return (long) query.getSingleResult();
    }

    @Override
    @Transactional
    public BookDTO processBookCreation(BookDTO book, MultipartFile image) {

        // Kiểm tra trùng lặp
        List<BookDTO> existingBooks = findBooksByTitleAndAuthorAndPublisher(
                book.getBookTitle(),
                book.getAuthor(),
                book.getPublisher());

        if (!existingBooks.isEmpty()) {
            throw new AppException(ErrorCode.BOOK_ALREADY_EXISTS);
        }

        // Lưu sách trước để có ID
        BookDTO savedBook = save(book);

        // Xử lý ảnh sau khi có ID
        if (image != null && !image.isEmpty()) {
            String imagePath = handleImageUpload(image, savedBook.getBookID());
            savedBook.setImage(imagePath);
            update(savedBook);
        }

        // Xử lý danh mục
        if (savedBook.getBookCategories() != null) {
            for (BookCategoryDTO category : savedBook.getBookCategories()) {
                if (category.getCatId() == null) {
                    throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
                }
                category.setBookId(savedBook);
                entityManager.persist(category);
            }
        }
        return savedBook;
    }

    @Override
    public String handleImageUpload(MultipartFile image, Integer bookId) {
        try {
            // Validate image extension
            validateImageExtension(image);

            // Prepare upload directory
            String uploadDir = System.getProperty("user.dir") + "/uploads/bookImage/";
            Path uploadPath = Paths.get(uploadDir);

            // Create directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique file name
            String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String fileName = String.format("book_%d_%d.%s", bookId, System.currentTimeMillis(), extension);

            // Save file
            try (InputStream inputStream = image.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/bookImage/" + fileName;

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    private void validateImageExtension(MultipartFile image) {
        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        if (extension == null) {
            throw new AppException(ErrorCode.INVALID_IMAGE_TYPE);
        }
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new AppException(ErrorCode.INVALID_IMAGE_TYPE);
        }
    }
}