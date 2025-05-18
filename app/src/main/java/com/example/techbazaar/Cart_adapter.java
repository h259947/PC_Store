package com.example.techbazaar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Cart_adapter extends RecyclerView.Adapter<Cart_adapter.ProductViewHolder>{
    FirebaseFirestore items_db;
    private CollectionReference items_ref;

    private Context context;
    private List<Home_items> productList;

    public Cart_adapter(Context context, List<Home_items> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_items_view, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Home_items product = productList.get(position);
        holder.name.setText(product.getName());
        holder.price.setText(product.getPrice());
        Glide.with(context).load(product.getImgsrc()).into(holder.imageView);

        ArrayList<String> numbers = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            numbers.add(i + " db");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, numbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.select_number.setAdapter(adapter);
        holder.select_number.setSelection(0);

        items_db = FirebaseFirestore.getInstance();
        items_ref = items_db.collection("Users");

        holder.delete_button.setOnClickListener(v -> {
            String current_user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String current_anonym_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Home_items currentItem = productList.get(holder.getAdapterPosition());

            items_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                    User_features user_cart = docu.toObject(User_features.class);
                    if(user_cart.getEmail().equals(current_user_email) || user_cart.getUsername().equals(current_anonym_user)){
                        List<Home_items> cartItems = new ArrayList<>(user_cart.getCart_items());

                        for (int i = 0; i < cartItems.size(); i++) {
                            if (cartItems.get(i).getName().equals(currentItem.getName())) {
                                cartItems.remove(i);
                                break;
                            }
                        }

                        items_ref.document(docu.getId())
                                .update("cart_items", cartItems)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Sikeresen törölve a kosárból!", Toast.LENGTH_SHORT).show();
                                    productList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    ((Cart_activity) context).order();
                                    Long current_cart_counted = docu.getLong("cart_counted");
                                    items_ref.document(docu.getId()).update("cart_counted", current_cart_counted - 1);
                                    ((Cart_activity) context).countedItem();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        ImageView imageView;
        ImageButton delete_button;
        Spinner select_number;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            price = itemView.findViewById(R.id.item_price);
            imageView = itemView.findViewById(R.id.item_img);
            delete_button = itemView.findViewById(R.id.delete_from_cart);
            select_number = itemView.findViewById(R.id.spinner);
        }
    }
}