package com.example.localizationtesting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    private String nameEn;
    private String nameVi;
    private String nameFr;
    private String nameAr;
    private String nameDe;
    private String nameJa;
    private String nameRu;

    @Column(length = 2000)
    private String descriptionEn;

    @Column(length = 2000)
    private String descriptionVi;

    @Column(length = 2000)
    private String descriptionFr;

    @Column(length = 2000)
    private String descriptionAr;

    @Column(length = 2000)
    private String descriptionDe;

    @Column(length = 2000)
    private String descriptionJa;

    @Column(length = 2000)
    private String descriptionRu;

    private BigDecimal price;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String imageData;

    private String imageMimeType;

    private LocalDate releaseDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}