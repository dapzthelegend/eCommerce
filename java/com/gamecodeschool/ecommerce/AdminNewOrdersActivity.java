package com.gamecodeschool.ecommerce;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gamecodeschool.ecommerce.Model.Orders;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminNewOrdersActivity extends AppCompatActivity {
    private RecyclerView ordersList;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_orders);
        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        ordersList = (RecyclerView) findViewById(R.id.ordersList);
        ordersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Orders> options =
                new FirebaseRecyclerOptions.Builder<Orders>()
                .setQuery(ordersRef, Orders.class)
                .build();

        FirebaseRecyclerAdapter<Orders,AdminOrdersViewHolder> adapter =
                new FirebaseRecyclerAdapter<Orders, AdminOrdersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AdminOrdersViewHolder holder, final int position, @NonNull final Orders model) {
                        holder.txtPhoneNumber.setText("Phone Number: " + model.getPhone());
                        holder.txtAddressCity.setText("Shipping Address: " + model.getAddress() + ", " + model.getCity());
                        holder.txtUserName.setText("Name: " + model.getName());
                        holder.txtDateTime.setText("Order Time: " + model.getDate() + ", " + model.getTime());
                        holder.txtTotalPrice.setText("Total Price : " + model.getTotalAmount());
                        holder.btnViewOrders.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String UID = getRef(position).getKey();
                                Intent intent = new Intent(AdminNewOrdersActivity.this, AdminUserProductsActivity.class);
                                intent.putExtra("uid", UID);
                                startActivity(intent);
                            }
                        });
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = {
                                        "Yes",
                                        "No"
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminNewOrdersActivity.this);
                                builder.setTitle("Have you shipped these Products?");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which == 0){
                                            String UID = getRef(position).getKey();
                                            RemoveOrder(UID);
                                        }
                                        else{
                                            finish();
                                        }

                                    }
                                });
                                builder.show();

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public AdminOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.orders_layout, viewGroup, false);
                        return new AdminOrdersViewHolder(view);
                    }
                };
        ordersList.setAdapter(adapter);
        adapter.startListening();

    }

    private void RemoveOrder(String uid) {
        ordersRef.child(uid).removeValue();
        FirebaseDatabase.getInstance().getReference().child("Cart List").child("Admin View").child(uid).child("Products").removeValue();
    }


    public static class AdminOrdersViewHolder extends RecyclerView.ViewHolder{
        private TextView txtUserName, txtPhoneNumber, txtAddressCity, txtDateTime, txtTotalPrice;
        private Button btnViewOrders;
        public AdminOrdersViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = (TextView)itemView.findViewById(R.id.orderUserName);
            txtAddressCity = (TextView)itemView.findViewById(R.id.orderAddressCity);
            txtDateTime = (TextView)itemView.findViewById(R.id.orderDateTime);
            txtTotalPrice = (TextView)itemView.findViewById(R.id.orderTotalPrice);
            txtPhoneNumber = (TextView)itemView.findViewById(R.id.orderPhoneNumber);
            btnViewOrders = (Button) itemView.findViewById(R.id.btnViewOrderItems);


        }
    }

}
