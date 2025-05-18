package com.example.techbazaar;

public class Categories_items {
    private String category_name;
    private int category_img;

    public Categories_items() {}

    public Categories_items(String category_name, int category_img) {
        this.category_name = category_name;
        this.category_img = category_img;
    }

    public int getCategory_img() {return category_img;}
    public String getCategory_name() {return category_name;}
}
