package com.example.techbazaar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Reg_activity extends AppCompatActivity {
    FirebaseFirestore user_db;
    private CollectionReference user_ref;

    private static final int SK = 34788;
    EditText new_username, email, password_1, password_2;
    CheckBox ch_1, ch_2;

    private FirebaseAuth first_Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        int sk = getIntent().getIntExtra("SK", 0);
        if(sk != SK){
            finish();
        }

        new_username = findViewById(R.id.new_username);
        email = findViewById(R.id.email);
        password_1 = findViewById(R.id.password_1);
        password_2 = findViewById(R.id.password_2);
        ch_1 = findViewById(R.id.checkBox1);
        ch_2 = findViewById(R.id.checkBox2);

        first_Auth = FirebaseAuth.getInstance();
        user_db = FirebaseFirestore.getInstance();
        user_ref = user_db.collection("Users");
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
        else if (!cs1 || !cs2) Toast.makeText(Reg_activity.this,"A feltételek elfogadása kötelező!", Toast.LENGTH_SHORT).show();
        else {
            first_Auth.createUserWithEmailAndPassword(email_address, p1).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Reg_activity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                    user_ref.add(new User_features(email_address, username));
                    home();
                } else {
                    Toast.makeText(Reg_activity.this, "Sikertelen regisztráció: "
                            + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void ignore(View view) {finish();}

    private void home(){
        Intent home_intent = new Intent(this, Home_activity.class);
        startActivity(home_intent);
    }
}