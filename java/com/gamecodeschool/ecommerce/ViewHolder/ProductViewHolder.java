package com.gamecodeschool.ecommerce.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gamecodeschool.ecommerce.Interface.ItemClickListner;
import com.gamecodeschool.ecommerce.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtProductName, txtProductDescription, txtProductPrice;
    public ImageView imageView;
    public ItemClickListner listner;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.productImage);
        txtProductName = (TextView) itemView.findViewById(R.id.productName);
        txtProductDescription = (TextView) itemView.findViewById(R.id.productDescription);
        txtProductPrice = (TextView) itemView.findViewById(R.id.productPrice);
    }

    public void setItemClickListener(ItemClickListner listner){
        this.listner = listner;
    }

    @Override
    public void onClick(View v) {
        listner.onClick(v, getAdapterPosition(), false);

    }
}
