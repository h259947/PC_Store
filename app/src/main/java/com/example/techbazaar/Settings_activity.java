package com.example.techbazaar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class Settings_activity extends AppCompatActivity {
    FirebaseFirestore users_db;
    private CollectionReference users_ref;
    private FirebaseAuth first_Auth;

    private static final int REQUEST_CODE_PICK_IMAGE = 2;
    private static final int REQUEST_CODE_PERMISSION = 101;

    ImageView profileImage;
    TextView show_username;
    EditText user_full_name;
    EditText user_city;
    EditText user_postcode;
    EditText user_street;
    EditText user_mobil;

    RecyclerView category_view;
    Categories_adapter cadapter;
    private ArrayList<Categories_items> category_items;

    EditText new_username, email, password_1, password_2;
    CheckBox ch_1, ch_2;

    private FrameLayout circle;
    private TextView countn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (user.isAnonymous()) {
                setContentView(R.layout.activity_settings_anonymus);
            } else {
                setContentView(R.layout.activity_settings);

            }
        }

        category_view = findViewById(R.id.category_Recycler);
        category_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        category_items = new ArrayList<>();
        load_category_menu();
        cadapter = new Categories_adapter(this, category_items);
        category_view.setAdapter(cadapter);

        setSupportActionBar(findViewById(R.id.toolbar));

        show_username = findViewById(R.id.show_username);

        profileImage = findViewById(R.id.profile_image);

        user_full_name = findViewById(R.id.user_full_name);
        user_city = findViewById(R.id.user_city);
        user_postcode = findViewById(R.id.user_postcode);
        user_street = findViewById(R.id.user_street);
        user_mobil = findViewById(R.id.user_mobil);

        new_username = findViewById(R.id.new_username);
        email = findViewById(R.id.email);
        password_1 = findViewById(R.id.password_1);
        password_2 = findViewById(R.id.password_2);
        ch_1 = findViewById(R.id.checkBox1);
        ch_2 = findViewById(R.id.checkBox2);

        first_Auth = FirebaseAuth.getInstance();
        users_db = FirebaseFirestore.getInstance();
        users_ref = users_db.collection("Users");

        user_data_set();
        setProfileImage();
    }

    public void setProfileImage(){
        users_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
            String current_email_user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            for(QueryDocumentSnapshot p : queryDocumentSnapshots) {
                User_features user_data = p.toObject(User_features.class);
                if (user_data.getEmail().equals(current_email_user)) {
                    String imageUriString = user_data.getProfile_image_uri();

                    if (imageUriString == null || imageUriString.isEmpty()) {
                        profileImage.setImageResource(R.drawable.no_profile_image);
                        break;
                    }

                    Uri imageUri = Uri.parse(imageUriString);

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        if (inputStream != null) {
                            profileImage.setImageURI(imageUri);
                            inputStream.close();
                        } else {
                            profileImage.setImageResource(R.drawable.no_profile_image);
                        }
                    } catch (Exception e) {
                        profileImage.setImageResource(R.drawable.no_profile_image);
                        e.printStackTrace();
                    }
                    break;
                }
            }
        });
    }

    public void user_data_set(){
        users_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
            String current_email_user = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                User_features user_data = docu.toObject(User_features.class);
                if (user_data.getEmail().equals(current_email_user)) {
                    show_username.setText(user_data.getUsername());
                   if(user_data.getFullname() != null) user_full_name.setText(user_data.getFullname());
                   if(user_data.getCity() != null) user_city.setText(user_data.getCity());
                   if(user_data.getPostcode() != null) user_postcode.setText(user_data.getPostcode());
                   if(user_data.getStreet() != null) user_street.setText(user_data.getStreet());
                   if(user_data.getMobil() != null) user_mobil.setText(user_data.getMobil());
                   break;
                }
            }
        });
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
            Toast.makeText(Settings_activity.this, "Kijelentkezve!", Toast.LENGTH_SHORT).show();

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
            Intent cart_intent = new Intent(Settings_activity.this, Cart_activity.class);
            startActivity(cart_intent);
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public void countedItem(){
        users_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
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

    public void register(View view) {
        String username = new_username.getText().toString();
        String email_address = email.getText().toString();
        String p1 = password_1.getText().toString();
        String p2 = password_2.getText().toString();
        boolean cs1 = ch_1.isChecked();
        boolean cs2 = ch_2.isChecked();

        if (username.isEmpty()) new_username.setError("Felhasználónév megadása kötelező!");
        else if (email_address.isEmpty()) email.setError("Email cím megadása kötelező!");
        else if (p1.isEmpty()) password_1.setError("Nem adtál meg jelszót!");
        else if (p1.length() < 6) password_1.setError("A jelszónak legalább 6 karakternek kell lennie!");
        else if (!p1.equals(p2)) password_2.setError("Nem egyeznek a jelszók!");
        else if (!cs1 || !cs2) Toast.makeText(Settings_activity.this,"A feltételek elfogadása kötelező!", Toast.LENGTH_SHORT).show();
        else {
            first_Auth.createUserWithEmailAndPassword(email_address, p1).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Settings_activity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                    users_ref.add(new User_features(email_address, username));
                    go_settings();
                } else {
                    Toast.makeText(Settings_activity.this, "Sikertelen regisztráció: "
                            + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void go_settings(){
        Intent settings_intent = new Intent(this, Settings_activity.class);
        startActivity(settings_intent);
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

    public void save_user_data(View view) {
        users_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
            String current_email_user = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                User_features user_data = docu.toObject(User_features.class);
                if (user_data.getEmail().equals(current_email_user)) {
                    if(user_full_name != null) user_data.setFullname(user_full_name.getText().toString());
                    else user_data.setFullname("");
                    if(user_city != null) user_data.setCity(user_city.getText().toString());
                    else user_data.setFullname("");
                    if(user_postcode != null) user_data.setPostcode(user_postcode.getText().toString());
                    else user_data.setFullname("");
                    if(user_street != null) user_data.setStreet(user_street.getText().toString());
                    else user_data.setFullname("");
                    if(user_mobil != null) user_data.setMobil(user_mobil.getText().toString());
                    users_ref.document(docu.getId()).set(user_data)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(Settings_activity.this, "Adatok frissítve!", Toast.LENGTH_LONG).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(Settings_activity.this, "Hiba mentés közben!", Toast.LENGTH_LONG).show()
                            );
                    break;
                }
            }
        });
    }

    public void delete_user(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String current_email_user = currentUser.getEmail();

        users_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                User_features user_data = docu.toObject(User_features.class);
                if (user_data.getEmail().equals(current_email_user)) {
                    users_ref.document(docu.getId()).delete()
                            .addOnSuccessListener(aVoid -> currentUser.delete()
                                    .addOnSuccessListener(aVoid1 -> {
                                        Toast.makeText(Settings_activity.this, "Fiók törölve!", Toast.LENGTH_LONG).show();
                                        Intent Start_intent = new Intent(Settings_activity.this, Start_activity.class);
                                        Start_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(Start_intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(Settings_activity.this, "Hiba a fiók törlése közben!", Toast.LENGTH_LONG).show()))
                            .addOnFailureListener(e -> Toast.makeText(Settings_activity.this, "Hiba az adatok törlésekor!", Toast.LENGTH_LONG).show());
                    break;
                }
            }
        });
    }

    public void updateProfileImage(View view) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_PERMISSION);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION);
            } else {
                openGallery();
            }
        }

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            users_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
                String current_email_user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                for(QueryDocumentSnapshot p : queryDocumentSnapshots) {
                    User_features user_data = p.toObject(User_features.class);
                    if (user_data.getEmail().equals(current_email_user)) {
                        users_ref.document(p.getId()).update("profile_image_uri", imageUri.toString());
                        break;
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Engedély szükséges a galéria eléréséhez", Toast.LENGTH_SHORT).show();
        }
    }
}
