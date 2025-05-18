package com.example.techbazaar;

import java.util.List;

public class User_features {
        private String Email;
        private String username;
        private String fullname;
        private String city;
        private String postcode;
        private String mobil;
        private String street;
        private String profile_image_uri;
        private List<Home_items> cart_items;
        private List<Home_items> favorites;
        private long cart_counted;

    public User_features() {}

    public User_features(String email, String username) {
        this.Email = email;
        this.username = username;
        this.cart_counted = 0;
    }

    public long getCart_counted() {return cart_counted;}
    public void setCart_counted(int cart_counted) {this.cart_counted = cart_counted;}
    public String getCity() {return city;}
    public void setCity(String city) {this.city = city;}
    public String getMobil() {return mobil;}
    public void setMobil(String mobil) {this.mobil = mobil;}
    public String getPostcode() {return postcode;}
    public void setPostcode(String postcode) {this.postcode = postcode;}
    public String getStreet() {return street;}
    public void setStreet(String street) {this.street = street;}
    public String getEmail() {return Email;}
    public void setEmail(String email) {Email = email;}
    public String getFullname() {return fullname;}
    public void setFullname(String fullname) {this.fullname = fullname;}
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public List<Home_items> getCart_items() {return cart_items;}
    public void setCart_items(List<Home_items> cart_items) {this.cart_items = cart_items;}
    public List<Home_items> getFavorites() {return favorites;}
    public void setFavorites(List<Home_items> favorites) {this.favorites = favorites;}
    public String getProfile_image_uri() {return profile_image_uri;}
    public void setProfile_image_uri(String profile_image_uri) {this.profile_image_uri = profile_image_uri;}
}
