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
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            "b.bookID DESC", "b.publicationYear DESC", "sold DESC");

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
    public List<BookDTO> findBooksByTitleAndAuthorAndPublisher(
            String bookTitle,
            String author,
            String publisher,
            Integer excludeBookId // Thêm tham số để loại trừ bản ghi hiện tại
    ) {
        // Xây dựng câu query cơ bản
        StringBuilder jpql = new StringBuilder(
                "SELECT b FROM BookDTO b WHERE " +
                        "b.bookTitle = :bookTitle AND " +
                        "b.author = :author AND " +
                        "b.publisher = :publisher");

        // Thêm điều kiện loại trừ bookID nếu được cung cấp
        if (excludeBookId != null) {
            jpql.append(" AND b.bookID != :excludeBookId");
        }

        // Tạo query
        TypedQuery<BookDTO> query = entityManager.createQuery(jpql.toString(), BookDTO.class)
                .setParameter("bookTitle", bookTitle)
                .setParameter("author", author)
                .setParameter("publisher", publisher);

        // Thiết lập tham số excludeBookId nếu có
        if (excludeBookId != null) {
            query.setParameter("excludeBookId", excludeBookId);
        }

        // Thực thi và trả về kết quả
        List<BookDTO> result = query.getResultList();
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
            List<Integer> categoryIds,
            String sort,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String mainText) {

        // Bước 1: Lấy danh sách ID với điều kiện và phân trang
        List<Integer> bookIds = getFilteredBookIds(offset, pageSize, bookTitle, author, translator,
                publicationYear, isbn, bookStatus, categoryIds, sort, minPrice, maxPrice, mainText);

        // Bước 2: Fetch entity theo danh sách ID
        if (!bookIds.isEmpty()) {
            return fetchBooksByIds(bookIds, sort); // Thêm tham số
        }
        return new ArrayList<>();
    }

    private List<Integer> getFilteredBookIds(int offset,
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
            String mainText) {

        StringBuilder queryStr = new StringBuilder();

        if ("sold ASC".equals(sort) || "sold DESC".equals(sort)) {
            // Truy vấn cho sắp xếp theo sold: chọn thêm biểu thức tổng số bán (totalSold)
            queryStr.append("SELECT DISTINCT b.bookID, ");
            queryStr.append(
                    "       (SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetailDTO od WHERE od.bookID.bookID = b.bookID) AS totalSold ");
            queryStr.append("FROM BookDTO b ");
            queryStr.append("JOIN b.bookCategories bc JOIN bc.catId c ");
            queryStr.append("WHERE 1=1");
        } else {
            queryStr.append("SELECT DISTINCT b.bookID, b.bookPrice, b.publicationYear FROM BookDTO b ");
            queryStr.append("JOIN b.bookCategories bc JOIN bc.catId c ");
            queryStr.append("WHERE 1=1");
        }

        // Thêm các điều kiện lọc
        addConditions(queryStr, bookTitle, author, translator, publicationYear, isbn, bookStatus, categoryIds, minPrice,
                maxPrice, mainText);

        // Xử lý sắp xếp
        if ("sold ASC".equals(sort)) {
            queryStr.append(" ORDER BY totalSold ASC");
        } else if ("sold DESC".equals(sort)) {
            queryStr.append(" ORDER BY totalSold DESC");
        } else {
            handleSorting(queryStr, sort);
        }

        Query query = entityManager.createQuery(queryStr.toString());
        setParameters(query, bookTitle, author, translator, publicationYear, isbn, bookStatus, categoryIds, minPrice,
                maxPrice, mainText);

        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        // Lấy kết quả và chỉ lấy bookID (cột đầu tiên)
        List<Object[]> resultList = query.getResultList();
        return resultList.stream()
                .map(arr -> (Integer) arr[0])
                .collect(Collectors.toList());
    }

    private List<BookDTO> fetchBooksByIds(List<Integer> bookIds, String sort) {
        String jpql;
        if ("sold ASC".equals(sort) || "sold DESC".equals(sort)) {
            String order = "sold ASC".equals(sort) ? "ASC" : "DESC";
            jpql = "SELECT b FROM BookDTO b WHERE b.bookID IN :ids " +
                    "ORDER BY (SELECT COALESCE(SUM(od.quantity), 0) " +
                    "         FROM OrderDetailDTO od " +
                    "         WHERE od.bookID.bookID = b.bookID) " + order;
        } else {
            jpql = "SELECT b FROM BookDTO b WHERE b.bookID IN :ids";
            if (sort != null && ALLOWED_SORT_FIELDS.contains(sort)) {
                jpql += " ORDER BY " + convertSortField(sort);
            } else {
                jpql += " ORDER BY b.bookID DESC";
            }
        }
        List<BookDTO> books = entityManager.createQuery(jpql, BookDTO.class)
                .setParameter("ids", bookIds)
                .getResultList();

        // Tính tổng số lượng bán (totalSold) từ orderDetailList và gán vào đối tượng
        // BookDTO
        for (BookDTO book : books) {
            if (book.getOrderDetailList() != null && !book.getOrderDetailList().isEmpty()) {
                long total = book.getOrderDetailList()
                        .stream()
                        .filter(od -> od.getQuantity() != null)
                        .mapToLong(od -> od.getQuantity())
                        .sum();
                book.setTotalSold(total);
            } else {
                book.setTotalSold(0L);
            }
        }
        return books;
    }

    // Các phương thức helper
    private void addConditions(StringBuilder queryStr,
            String bookTitle,
            String author,
            String translator,
            Integer publicationYear,
            String isbn,
            Integer bookStatus,
            List<Integer> categoryIds,
            BigDecimal minPrice, // Thêm tham số
            BigDecimal maxPrice,
            String mainText) {
        if (bookTitle != null && !bookTitle.isEmpty()) {
            queryStr.append(" AND LOWER(b.bookTitle) LIKE LOWER(:bookTitle)");
        }
        if (author != null && !author.isEmpty()) {
            queryStr.append(" AND LOWER(b.author) LIKE LOWER(:author)");
        }
        if (translator != null && !translator.isEmpty()) {
            queryStr.append(" AND LOWER(b.translator) LIKE LOWER(:translator)");
        }
        if (publicationYear != null) {
            queryStr.append(" AND b.publicationYear = :publicationYear");
        }
        if (isbn != null && !isbn.isEmpty()) {
            queryStr.append(" AND b.isbn = :isbn");
        }
        if (bookStatus != null) {
            queryStr.append(" AND b.bookStatus = :bookStatus");
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            queryStr.append(" AND c.catID IN :categoryIds");
        }
        if (minPrice != null) {
            queryStr.append(" AND b.bookPrice >= :minPrice");
        }
        if (maxPrice != null) {
            queryStr.append(" AND b.bookPrice <= :maxPrice");
        }
        if (mainText != null && !mainText.isEmpty()) {
            queryStr.append(" AND LOWER(b.bookTitle) LIKE LOWER(:mainText)");
        }
    }

    private void handleSorting(StringBuilder queryStr, String sort) {
        if (sort != null && ALLOWED_SORT_FIELDS.contains(sort)) {
            queryStr.append(" ORDER BY ").append(convertSortField(sort));
        } else {
            queryStr.append(" ORDER BY b.bookID DESC");
        }
    }

    private String convertSortField(String sort) {
        // Chuyển đổi tên sort từ entity property sang field trong HQL
        return sort.replace("b.", ""); // Ví dụ: "bookPrice DESC" -> "b.bookPrice DESC"
    }

    private void setParameters(Query query,
            String bookTitle,
            String author,
            String translator,
            Integer publicationYear,
            String isbn,
            Integer bookStatus,
            List<Integer> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String mainText) {
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
        if (categoryIds != null && !categoryIds.isEmpty()) {
            query.setParameter("categoryIds", categoryIds);
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        if (mainText != null && !mainText.isEmpty()) {
            query.setParameter("mainText", "%" + mainText + "%");
        }
    }

    @Override
    public long countBooksWithConditions(String bookTitle,
            String author,
            String translator,
            Integer publicationYear,
            String isbn,
            Integer bookStatus,
            List<Integer> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String mainText) {

        StringBuilder queryStr = new StringBuilder(
                "SELECT COUNT(DISTINCT b.bookID) FROM BookDTO b " +
                        "JOIN b.bookCategories bc " +
                        "JOIN bc.catId c " + // Alias 'c' đại diện cho CategoryDTO
                        "WHERE 1=1");

        // Thêm điều kiện (FIX: dùng c.catID thay vì bc.catId.catID)
        if (bookTitle != null && !bookTitle.isEmpty()) {
            queryStr.append(" AND LOWER(b.bookTitle) LIKE LOWER(:bookTitle)");
        }
        if (author != null && !author.isEmpty()) {
            queryStr.append(" AND LOWER(b.author) LIKE LOWER(:author)");
        }
        if (translator != null && !translator.isEmpty()) {
            queryStr.append(" AND LOWER(b.translator) LIKE LOWER(:translator)");
        }
        if (publicationYear != null) {
            queryStr.append(" AND b.publicationYear = :publicationYear");
        }
        if (isbn != null && !isbn.isEmpty()) {
            queryStr.append(" AND b.isbn = :isbn");
        }
        if (bookStatus != null) {
            queryStr.append(" AND b.bookStatus = :bookStatus");
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            queryStr.append(" AND c.catID IN :categoryIds");
        }
        if (minPrice != null) {
            queryStr.append(" AND b.bookPrice >= :minPrice");
        }
        if (maxPrice != null) {
            queryStr.append(" AND b.bookPrice <= :maxPrice");
        }
        if (mainText != null && !mainText.isEmpty()) {
            queryStr.append(" AND LOWER(b.bookTitle) LIKE LOWER(:mainText)");
        }

        // Tạo query
        Query query = entityManager.createQuery(queryStr.toString());

        // Đặt tham số (FIX: chỉ set 1 lần cho categoryIds)
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
        if (categoryIds != null && !categoryIds.isEmpty()) {
            query.setParameter("categoryIds", categoryIds); // Đúng tên tham số
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        if (mainText != null && !mainText.isEmpty()) {
            query.setParameter("mainText", "%" + mainText + "%");
        }

        return (long) query.getSingleResult();
    }

    @Override
    @Transactional
    public BookDTO processBookCreation(BookDTO book, MultipartFile image) {

        // Kiểm tra trùng lặp
        List<BookDTO> existingBooks = findBooksByTitleAndAuthorAndPublisher(
                book.getBookTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getBookID());

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

    @Override
    @Transactional
    public void deleteBookWithCategories(Integer bookId) {
        // Xóa danh mục trước
        Query deleteCategories = entityManager.createNativeQuery(
                "DELETE FROM Book_Category WHERE book_id = :bookId");
        deleteCategories.setParameter("bookId", bookId);
        int deletedCategories = deleteCategories.executeUpdate();

        // Xóa sách
        Query deleteBook = entityManager.createNativeQuery(
                "DELETE FROM Book WHERE bookID = :bookId");
        deleteBook.setParameter("bookId", bookId);
        int deletedBooks = deleteBook.executeUpdate();

        if (deletedBooks == 0) {
            throw new AppException(ErrorCode.BOOK_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public BookDTO processBookUpdate(BookDTO updatedBook, MultipartFile image) {
        try {
            // Kiểm tra sách tồn tại
            BookDTO existingBook = find(updatedBook.getBookID());
            if (existingBook == null) {
                throw new AppException(ErrorCode.BOOK_NOT_FOUND);
            }

            // Kiểm tra trùng lặp (trừ chính nó)
            List<BookDTO> existingBooks = findBooksByTitleAndAuthorAndPublisher(
                    updatedBook.getBookTitle(),
                    updatedBook.getAuthor(),
                    updatedBook.getPublisher(),
                    updatedBook.getBookID());
            if (!existingBooks.isEmpty()) {
                throw new AppException(ErrorCode.BOOK_ALREADY_EXISTS);
            }

            // Cập nhật thông tin cơ bản
            // Cập nhật tất cả các trường dữ liệu từ updatedBook sang existingBook
            existingBook.setBookTitle(updatedBook.getBookTitle());
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setTranslator(updatedBook.getTranslator());
            existingBook.setPublisher(updatedBook.getPublisher());
            existingBook.setPublicationYear(updatedBook.getPublicationYear());
            existingBook.setIsbn(updatedBook.getIsbn());
            existingBook.setBookDescription(updatedBook.getBookDescription());
            existingBook.setHardcover(updatedBook.getHardcover());
            existingBook.setDimension(updatedBook.getDimension());
            existingBook.setWeight(updatedBook.getWeight());
            existingBook.setBookPrice(updatedBook.getBookPrice());
            existingBook.setBookQuantity(updatedBook.getBookQuantity());
            existingBook.setBookStatus(updatedBook.getBookStatus());

            // Xử lý ảnh
            if (image != null && !image.isEmpty()) {
                String imagePath = handleImageUpload(image, existingBook.getBookID());
                existingBook.setImage(imagePath);
            }

            // 5. Xóa toàn bộ danh mục cũ
            entityManager.createQuery("DELETE FROM BookCategoryDTO bc WHERE bc.bookId = :bookId")
                    .setParameter("bookId", existingBook)
                    .executeUpdate();

            // 6. Thêm danh mục mới
            if (updatedBook.getBookCategories() != null && !updatedBook.getBookCategories().isEmpty()) {
                updatedBook.getBookCategories().forEach(categoryDTO -> {
                    BookCategoryDTO newCategory = new BookCategoryDTO();
                    newCategory.setBookId(existingBook); // Liên kết với sách hiện tại
                    newCategory.setCatId(categoryDTO.getCatId()); // Lấy từ DTO truyền vào
                    entityManager.persist(newCategory); // INSERT danh mục mới
                });
            }

            // 7. Đảm bảo thay đổi được lưu
            entityManager.flush();
            return entityManager.merge(existingBook);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<BookDTO> findBooksByCategory(int catID) {
        TypedQuery<BookDTO> query = entityManager.createQuery(
                "SELECT b FROM BookDTO b JOIN b.bookCategories bc WHERE bc.catId.catID = :catID", BookDTO.class);
        query.setParameter("catID", catID);

        return query.getResultList();
    }

    public List<BookDTO> findBooksByCategoryIds(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        TypedQuery<BookDTO> query = entityManager.createQuery(
                "SELECT b FROM BookDTO b " +
                        "JOIN b.bookCategories bc " +
                        "WHERE bc.catId.catID IN :cat_id",
                BookDTO.class);

        query.setParameter("cat_id", categoryIds);

        return query.getResultList();
    }
}