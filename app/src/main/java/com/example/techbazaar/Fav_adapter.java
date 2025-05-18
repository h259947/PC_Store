package com.example.techbazaar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class Fav_adapter extends RecyclerView.Adapter<Fav_adapter.ProductViewHolder>{
    FirebaseFirestore items_db;
    private CollectionReference items_ref;

    private Context context;
    private List<Home_items> productList;

    public Fav_adapter(Context context, List<Home_items> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fav_items_view, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Home_items product = productList.get(position);
        holder.name.setText(product.getName());
        holder.desc.setText(product.getDesc());
        holder.price.setText(product.getPrice());
        Glide.with(context).load(product.getImgsrc()).into(holder.imageView);

        items_db = FirebaseFirestore.getInstance();
        items_ref = items_db.collection("Users");

        holder.cart_button.setOnClickListener(v -> {
            String current_user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String current_anonym_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Home_items currentItem = productList.get(holder.getAdapterPosition());

            items_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
            boolean yet_in_cart = false;
            for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                User_features user_cart = docu.toObject(User_features.class);
                if(user_cart.getEmail().equals(current_user_email) || user_cart.getUsername().equals(current_anonym_user)){
                    if(user_cart.getCart_items() != null){
                        for(Home_items i : user_cart.getCart_items()) {
                            if (i.getName().equals(currentItem.getName())){
                                yet_in_cart = true;
                                break;
                            }
                        }
                    }
                    if(!yet_in_cart) {
                        items_ref.document(docu.getId())
                                .update("cart_items", com.google.firebase.firestore.FieldValue.arrayUnion(currentItem))
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Hozzáadva a kosárhoz!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        Long current_cart_counted = docu.getLong("cart_counted");
                        items_ref.document(docu.getId()).update("cart_counted", current_cart_counted + 1);
                        ((Favorite_activity) context).countedItem();
                    }
                    else {
                        Toast.makeText(context, "A terméket már tartalmazza a kosarad!", Toast.LENGTH_SHORT).show();
                        break;
                    }

                }
            }
        });
    });

        holder.delete_fav_button.setOnClickListener(v -> {
            String current_user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String current_anonym_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Home_items currentItem = productList.get(holder.getAdapterPosition());

            items_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                    User_features user_fav = docu.toObject(User_features.class);
                    if(user_fav.getEmail().equals(current_user_email) || user_fav.getUsername().equals(current_anonym_user)){
                        List<Home_items> favtItems = new ArrayList<>(user_fav.getFavorites());

                        for (int i = 0; i < favtItems.size(); i++) {
                            if (favtItems.get(i).getName().equals(currentItem.getName())) {
                                favtItems.remove(i);
                                break;
                            }
                        }

                        items_ref.document(docu.getId())
                                .update("favorites", favtItems)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Sikeresen törölve a kedvencekből!", Toast.LENGTH_SHORT).show();
                                    productList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    ((Favorite_activity)context).empty_fav();
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
        TextView name, price, desc;
        ImageView imageView;
        ImageButton delete_fav_button, cart_button;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            desc = itemView.findViewById(R.id.item_description);
            price = itemView.findViewById(R.id.item_price);
            imageView = itemView.findViewById(R.id.item_img);
            cart_button = itemView.findViewById(R.id.add_cart);
            delete_fav_button = itemView.findViewById(R.id.delete_from_favorite);
        }
    }
}