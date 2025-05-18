package com.example.techbazaar;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Favorite_activity extends AppCompatActivity {
    FirebaseFirestore fav_db;
    private CollectionReference fav_items_ref;

    TextView empty_fav;

    RecyclerView fav_view;
    RecyclerView category_view;

    private ArrayList<Home_items> fav_items;
    private ArrayList<Categories_items> category_items;

    private Fav_adapter fadapter;
    Categories_adapter cadapter;

    private FrameLayout circle;
    private TextView countn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        setSupportActionBar(findViewById(R.id.toolbar));

        fav_view = findViewById(R.id.favorite_content);
        category_view = findViewById(R.id.category_Recycler);

        fav_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        category_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        fav_items = new ArrayList<>();
        category_items = new ArrayList<>();

        fav_db = FirebaseFirestore.getInstance();
        fav_items_ref = fav_db.collection("Users");

        load_category_menu();
        load_favorites_with_firestore();

        fadapter = new Fav_adapter(this, fav_items);
        cadapter = new Categories_adapter(this, category_items);

        fav_view.setAdapter(fadapter);
        category_view.setAdapter(cadapter);

        empty_fav = findViewById(R.id.empty_fav);
    }

    private void load_favorites_with_firestore() {
        fav_items.clear();
        fav_items_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
            String current_email_user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String current_anonym_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

            for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                User_features user_favorites = docu.toObject(User_features.class);
                if (user_favorites.getEmail().equals(current_email_user) || user_favorites.getUsername().equals(current_anonym_user)) {
                    if(user_favorites.getFavorites() != null){
                        for(Home_items i : user_favorites.getFavorites()) {
                            if (i != null) fav_items.add(i);
                        }
                    }
                }
            }
            if(fav_items.isEmpty()) empty_fav.setText(R.string.no_fav);
            fadapter.notifyDataSetChanged();
        });
    }

    public void empty_fav(){
        if(fav_items.isEmpty()) empty_fav.setText(R.string.no_fav);
    }

    private void load_category_menu(){
        String[] citems_name = getResources().getStringArray(R.array.category_items_name);
        TypedArray citems_images = getResources().obtainTypedArray(R.array.category_images);

        category_items.clear();

        for (int i = 0; i < citems_name.length;i++){
            category_items.add(new Categories_items(citems_name[i],
                    citems_images.getResourceId(i,0)));
        }

        citems_images.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.cart) {
            Intent cart_intent = new Intent(this, Cart_activity.class);
            startActivity(cart_intent);
            return true;
        }
        else if (item.getItemId() == R.id.settings) {
            Intent settings_intent = new Intent(this, Settings_activity.class);
            startActivity(settings_intent);
            return true;
        }
        else if (item.getItemId() == R.id.fav){
            Intent favorite_intent = new Intent(this, Favorite_activity.class);
            startActivity(favorite_intent);
            return true;
        }
        else if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(Favorite_activity.this, "Kijelentkezve!", Toast.LENGTH_SHORT).show();

            Intent Start_intent = new Intent(this, Start_activity.class);
            Start_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(Start_intent);

            finish();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout cview = (FrameLayout) alertMenuItem.getActionView();

        circle = cview.findViewById(R.id.red_circle);
        countn = cview.findViewById(R.id.count_item);

        countedItem();
        cview.setOnClickListener(v -> {
            Intent cart_intent = new Intent(Favorite_activity.this, Cart_activity.class);
            startActivity(cart_intent);
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public void countedItem(){
        fav_items_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
            String current_email_user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String current_anonym_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

            for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                User_features user_cart = docu.toObject(User_features.class);
                if (user_cart.getEmail().equals(current_email_user) || user_cart.getUsername().equals(current_anonym_user)) {
                    if(user_cart.getCart_counted() > 0){
                        countn.setText(String.valueOf(user_cart.getCart_counted()));
                        circle.setVisibility(View.VISIBLE);
                    }
                    else circle.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        });
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent protected_intent = new Intent(this, Start_activity.class);
            protected_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(protected_intent);
            finish();
        }
    }
    public void clickHome(View view) {
        Intent home = new Intent(this, Home_activity.class);
        startActivity(home);
    }

}
