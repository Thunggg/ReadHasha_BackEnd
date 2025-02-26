package com.example.thuan.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.awt.print.Book;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "book_category")
public class BookCategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "book_cate_id")
    private Integer bookCateId;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "bookID")
    @JsonBackReference
    private BookDTO bookId;

    @ManyToOne
    @JoinColumn(name = "cat_id", referencedColumnName = "catID")
    private CategoryDTO catId;

}