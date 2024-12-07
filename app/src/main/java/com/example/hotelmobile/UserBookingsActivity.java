package com.example.hotelmobile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelmobile.adapter.BookingAdapter;
import com.example.hotelmobile.model.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private TextView tvNoBookings;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bookings);

        rvBookings = findViewById(R.id.rv_bookings);
        tvNoBookings = findViewById(R.id.tv_no_bookings);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList);
        rvBookings.setAdapter(bookingAdapter);

        loadUserBookings();
    }

    private void loadUserBookings() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = String.valueOf(sharedPreferences.getInt("user_id", -1));
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference bookingsRef = FirebaseDatabase.getInstance("https://hotelmobile-d180a-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("bookings");

        bookingsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        bookingList.clear();
                        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Map<String, Object> data = (Map<String, Object>) snapshot.getValue();

                            if (data != null) {
                                try {
                                    Booking booking = new Booking();
                                    booking.setInvoiceCode((String) data.get("invoiceCode"));
                                    booking.setUserId((String) data.get("userId"));
                                    booking.setHotelName((String) data.get("hotelName"));
                                    booking.setRoomName((String) data.get("roomName"));
                                    booking.setPaymentStatus((String) data.get("paymentStatus"));
                                    booking.setTotalPrice(((Long) data.get("totalPrice")).intValue());

                                    // Parse startDate và endDate
                                    String startDateStr = (String) data.get("startDate");
                                    String endDateStr = (String) data.get("endDate");

                                    if (startDateStr != null) {
                                        String fixedStartDate = startDateStr.replaceAll("([+-]\\d{2})(\\d{2})$", "$1:$2");
                                        booking.setStartDate(iso8601Format.parse(fixedStartDate));
                                    }
                                    if (endDateStr != null) {
                                        String fixedEndDate = endDateStr.replaceAll("([+-]\\d{2})(\\d{2})$", "$1:$2");
                                        booking.setEndDate(iso8601Format.parse(fixedEndDate));
                                    }

                                    bookingList.add(booking);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (bookingList.isEmpty()) {
                            rvBookings.setVisibility(View.GONE);
                            tvNoBookings.setVisibility(View.VISIBLE);
                        } else {
                            rvBookings.setVisibility(View.VISIBLE);
                            tvNoBookings.setVisibility(View.GONE);
                            bookingAdapter.notifyDataSetChanged();
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(UserBookingsActivity.this, "Failed to load bookings.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
