package com.example.techbazaar;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class Cart_activity extends AppCompatActivity {
    FirebaseFirestore cart_item_db;
    private CollectionReference cart_items_ref;

    RecyclerView cart_view;
    RecyclerView category_view;

    Cart_adapter ciadapter;
    Categories_adapter cadapter;

    private ArrayList<Home_items> cart_items;
    private ArrayList<Categories_items> category_items;

    TextView empty_cart;
    private FrameLayout circle;
    private TextView countn;

    private Button order_items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        empty_cart = findViewById(R.id.empty_cart);
        order_items = findViewById(R.id.order_item);

        cart_view = findViewById(R.id.cart_items);
        category_view = findViewById(R.id.category_Recycler);

        cart_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        category_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        cart_items = new ArrayList<>();
        category_items = new ArrayList<>();

        cart_item_db = FirebaseFirestore.getInstance();
        cart_items_ref = cart_item_db.collection("Users");

        load_category_menu();
        load_cart_with_firestore();

        ciadapter = new Cart_adapter(this, cart_items);
        cadapter = new Categories_adapter(this, category_items);

        cart_view.setAdapter(ciadapter);
        category_view.setAdapter(cadapter);

        setSupportActionBar(findViewById(R.id.toolbar));
    }

    private void load_cart_with_firestore() {
        cart_items.clear();
        cart_items_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
            String current_email_user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String current_anonym_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

            for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                User_features user_cart = docu.toObject(User_features.class);
                if (user_cart.getEmail().equals(current_email_user) || user_cart.getUsername().equals(current_anonym_user)) {
                    if(user_cart.getCart_items() != null){
                        for(Home_items i : user_cart.getCart_items()) {
                            if (i != null) cart_items.add(i);
                        }
                    }
                }
            }
            if(cart_items.isEmpty()) {
                empty_cart.setText(R.string.empty_cart);
                order_items.setVisibility(View.GONE);
            }
            ciadapter.notifyDataSetChanged();
        });
    }

    public void order(){
        if(cart_items.isEmpty()) {
            empty_cart.setText(R.string.empty_cart);
            order_items.setVisibility(View.GONE);
        }
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
            Toast.makeText(Cart_activity.this, "Kijelentkezve!", Toast.LENGTH_SHORT).show();

            Intent Start_intent = new Intent(this, Start_activity.class);
            Start_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(Start_intent);

            finish();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout cview = (FrameLayout) alertMenuItem.getActionView();

        circle = cview.findViewById(R.id.red_circle);
        countn = cview.findViewById(R.id.count_item);

        countedItem();
        cview.setOnClickListener(v -> {
            Intent cart_intent = new Intent(Cart_activity.this, Cart_activity.class);
            startActivity(cart_intent);
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public void countedItem(){
        cart_items_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
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
