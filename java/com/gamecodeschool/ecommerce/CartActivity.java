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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gamecodeschool.ecommerce.Model.Cart;
import com.gamecodeschool.ecommerce.Prevalent.Prevalent;
import com.gamecodeschool.ecommerce.ViewHolder.CartViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button btnNextCart;
    private TextView txtTotalAmount, txtMessage;
    private int orderTotalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = (RecyclerView) findViewById(R.id.cartList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        btnNextCart = (Button) findViewById(R.id.btnNextProcess);
        txtTotalAmount = (TextView) findViewById(R.id.cartProductPrice);

        txtMessage = (TextView) findViewById(R.id.message1);


        btnNextCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, ConfirmFinalActivity.class);
                intent.putExtra("Total Price", String.valueOf(orderTotalPrice));
                startActivity(intent);
                finish();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        checkOrderStat();

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        FirebaseRecyclerOptions<Cart> options =
                new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View")
                        .child(Prevalent.currentUser.getPhone()).child("Products"), Cart.class).build();

        FirebaseRecyclerAdapter<Cart,CartViewHolder> adapter =
                new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model) {
                       holder.txtProductQuantity.setText("Quantity = " + model.getQuantity());
                       holder.txtProductPrice.setText("Price = $" + model.getPrice() );
                       holder.txtProductName.setText(model.getName());


                       int oneProductTprice = ((Integer.valueOf(model.getPrice()))) * Integer.valueOf(model.getQuantity());
                       orderTotalPrice += oneProductTprice;
                       txtTotalAmount.setText("Total Amount = $" + orderTotalPrice );
                       holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CharSequence option[] =  new CharSequence[]
                                    {
                                            "Edit",
                                            "Remove"
                                    };
                            AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                            builder.setTitle("Cart Options");

                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(which == 0){
                                        Intent intent = new Intent (CartActivity.this, ProductDetailsActivity.class);
                                         intent.putExtra("pid", model.getPid());
                                         startActivity(intent);
                                    }


                                    if(which == 1){
                                        cartListRef.child("User View")
                                                .child(Prevalent.currentUser.getPhone())
                                                .child("Products")
                                                .child(model.getPid())
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(CartActivity.this, "item removed from cart", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                            builder.show();
                        }
                    });
                }

                    @NonNull
                    @Override
                    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cart_item_layout, viewGroup, false);
                        CartViewHolder holder = new CartViewHolder(view);
                        return holder;
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void checkOrderStat(){
        DatabaseReference orderRef;
        orderRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentUser.getPhone());

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String shippingstate = dataSnapshot.child("state").getValue().toString();
                    String username = dataSnapshot.child("name").getValue().toString();

                    if(shippingstate.equals("shipped")){
                        txtTotalAmount.setText("Awaiting order");
                        recyclerView.setVisibility(View.GONE);
                        txtMessage.setVisibility(View.VISIBLE);
                        btnNextCart.setVisibility(View.GONE);
                    }
                    else  if(shippingstate.equals("not shipped")){
                        txtTotalAmount.setText("Awaiting order");
                        recyclerView.setVisibility(View.GONE);
                        txtMessage.setVisibility(View.VISIBLE);
                        btnNextCart.setVisibility(View.GONE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
