package com.reactive.dc.client.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductDTO {

    private String id;

    private String name;

    private String price;

    private LocalDate createAt;

    private String photo;

    private  CategoryDTO category;
}
