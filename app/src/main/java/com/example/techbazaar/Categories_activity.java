package com.example.techbazaar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Categories_activity extends AppCompatActivity {
    FirebaseFirestore category_item_db;
    private CollectionReference category_items_ref;
    private CollectionReference cart_items_ref;

    RecyclerView main_items_view;
    RecyclerView category_view;

    Categories_adapter cadapter;
    Mainc_adapter iadapter;

    private ArrayList<Categories_items> category_items;
    private ArrayList<Home_items> main_items;

    private FrameLayout circle;
    private TextView countn;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String category_name = getIntent().getStringExtra("CATEGORY_NAME");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        setSupportActionBar(findViewById(R.id.toolbar));

        category_view = findViewById(R.id.category_Recycler);
        main_items_view = findViewById(R.id.content);

        category_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        main_items_view.setLayoutManager(new GridLayoutManager(this, 2));

        category_items = new ArrayList<>();
        main_items = new ArrayList<>();

        category_item_db = FirebaseFirestore.getInstance();
        category_items_ref = category_item_db.collection("Category_items");
        cart_items_ref = category_item_db.collection("Users");

        load_items_with_firestore(category_name);
        load_category_menu();

        cadapter = new Categories_adapter(this, category_items);
        iadapter = new Mainc_adapter(this, main_items);
        category_view.setAdapter(cadapter);
        main_items_view.setAdapter(iadapter);

    }

    private void load_items_with_firestore(String ctype) {
        main_items.clear();
        category_items_ref.orderBy("name").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                Home_items citem = docu.toObject(Home_items.class);
                if (citem.getName().startsWith(ctype.toLowerCase()))
                {
                    citem.setName(citem.getName().substring(ctype.length()+1));
                    main_items.add(citem);
                }
            }
            if (main_items.isEmpty()) {
                load_data(ctype);
                load_items_with_firestore(ctype);
            }
            iadapter.notifyDataSetChanged();
        });
    }

    private void load_data(String type) {
        String[] items_name = getResources().getStringArray(R.array.items_in_category_name);
        String[] items_description = getResources().getStringArray(R.array.items_in_category_description);
        String[] items_price = getResources().getStringArray(R.array.items_in_category_prices);
        TypedArray items_images = getResources().obtainTypedArray(R.array.items_in_category_images);
        TypedArray items_rated = getResources().obtainTypedArray(R.array.items_in_category_rates);

        for (int i = 0; i < items_name.length;i++){
            if(items_name[i].startsWith(type.toLowerCase())){
                category_items_ref.add(new Home_items(items_name[i],
                        items_description[i],
                        items_price[i], items_images.getResourceId(i,0),
                        items_rated.getFloat(i, 0)));
           }
        }
        items_images.recycle();
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

        MenuItem search_item = menu.findItem(R.id.search);
        SearchView search_view = (SearchView) search_item.getActionView();

        assert search_view != null;
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                iadapter.getFilter().filter(newText);
                return true;
            }
        });
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
            Toast.makeText(Categories_activity.this, "Kijelentkezve!", Toast.LENGTH_SHORT).show();

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
            Intent cart_intent = new Intent(Categories_activity.this, Cart_activity.class);
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