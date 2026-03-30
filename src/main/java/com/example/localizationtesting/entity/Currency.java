package com.example.localizationtesting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "currencies")
@Getter
@Setter
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String name;

    private String symbol;

    private String localeCode;

    @Column(nullable = false)
    private BigDecimal exchangeRateToDefault;

    @Column(nullable = false)
    private int decimalDigits;

    @Column(nullable = false)
    private String symbolPosition;

    private boolean active;

    @Column(nullable = false)
    private boolean defaultCurrency;
}