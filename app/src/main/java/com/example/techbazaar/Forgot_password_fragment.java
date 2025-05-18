package com.example.techbazaar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Forgot_password_fragment extends DialogFragment {
    FirebaseFirestore email_from_db;
    private CollectionReference users_email_ref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText emailInput = view.findViewById(R.id.email_input);
        Button cancel = view.findViewById(R.id.btn_cancel);
        Button reset = view.findViewById(R.id.btn_reset);

        email_from_db = FirebaseFirestore.getInstance();
        users_email_ref = email_from_db.collection("Users");
        cancel.setOnClickListener(v -> dismiss());

        reset.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            users_email_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
                String user_dbemail = "no_email";
                for(QueryDocumentSnapshot i : queryDocumentSnapshots){
                    User_features users = i.toObject(User_features.class);
                        if (users.getEmail().equals(email)){
                            user_dbemail = email;
                            break;
                        }
                }

                if(email.isEmpty()) emailInput.setError("Kérlek, add meg az email címet!");
                else if (!user_dbemail.equals(email)) emailInput.setError("Nem létező email címet adtál meg!");
                else {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Email elküldve!", Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "Hiba történt!", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}