package com.example.techbazaar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
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

public class Mainc_adapter extends RecyclerView.Adapter<Mainc_adapter.ProductViewHolder> implements Filterable {
    FirebaseFirestore items_db;
    private CollectionReference items_ref;

    private Context context;
    private List<Home_items> productList;
    private List<Home_items> productListFiltered;

    public Mainc_adapter(Context context, List<Home_items> productList) {
        this.context = context;
        this.productList = productList;
        this.productListFiltered = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_items, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Home_items product = productList.get(position);
        holder.name.setText(product.getName());
        holder.price.setText(product.getPrice());
        holder.description.setText(product.getDesc());
        holder.rate.setRating(product.getRate());
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
                            ((Categories_activity) context).countedItem();
                        }
                        else {
                            Toast.makeText(context, "A terméket már tartalmazza a kosarad!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
            });
        });
        holder.favoriteButton.setOnClickListener(v -> {
            String current_user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String current_anonym_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Home_items currentItem = productList.get(holder.getAdapterPosition());

            items_ref.get().addOnSuccessListener(queryDocumentSnapshots -> {
                boolean yet_in_fav = false;
                for (QueryDocumentSnapshot docu : queryDocumentSnapshots) {
                    User_features user_fav = docu.toObject(User_features.class);
                    if(user_fav.getEmail().equals(current_user_email) || user_fav.getUsername().equals(current_anonym_user)){
                        if(user_fav.getFavorites() != null){
                            for(Home_items i : user_fav.getFavorites()) {
                                if (i.getName().equals(currentItem.getName())){
                                    yet_in_fav = true;
                                    break;
                                }
                            }
                        }
                        if(!yet_in_fav) {
                            items_ref.document(docu.getId())
                                    .update("favorites", com.google.firebase.firestore.FieldValue.arrayUnion(currentItem))
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Hozzáadva a kedvencekhez!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                        else {
                            Toast.makeText(context, "A terméket már tartalmazza a kedvencek!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Home_items> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productListFiltered);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Home_items item : productListFiltered) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            productList = (List<Home_items>)filterResults.values;
            notifyDataSetChanged();
        }
    };

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, description;
        ImageView imageView;
        ImageButton favoriteButton;
        ImageButton cart_button;
        RatingBar rate;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            price = itemView.findViewById(R.id.item_price);
            description = itemView.findViewById(R.id.item_description);
            imageView = itemView.findViewById(R.id.item_img);
            rate = itemView.findViewById(R.id.item_rate);
            favoriteButton = itemView.findViewById(R.id.favorite);
            cart_button = itemView.findViewById(R.id.add_cart);
        }
    }
}