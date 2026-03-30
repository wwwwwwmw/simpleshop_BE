package com.example.localizationtesting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "languages")
@Getter
@Setter
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String name;

    @Column(nullable = false)
    private String localeCode;

    @Column(nullable = false)
    private boolean rtl;

    private boolean active;

    @Column(nullable = false)
    private boolean defaultLanguage;
}