package com.example.hotelmobile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelmobile.adapter.RoomInHotelAdapter;
import com.example.hotelmobile.model.Room;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomList extends AppCompatActivity {
    private RecyclerView recyclerViewRooms;
    private RoomInHotelAdapter adapter;
    private List<Room> roomList;
    private int hotelId; // ID của khách sạn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_room_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nhận hotelId từ Intent
        hotelId = getIntent().getIntExtra("hotelId", -1);

        if (hotelId == -1) {
            Toast.makeText(this, "Invalid hotel ID!", Toast.LENGTH_SHORT).show();
            finish(); // Thoát nếu không có hotelId
            return;
        }

        // Initialize RecyclerView and Room list
        recyclerViewRooms = findViewById(R.id.recyclerViewRooms);
        recyclerViewRooms.setLayoutManager(new LinearLayoutManager(this));
        roomList = new ArrayList<>();
        adapter = new RoomInHotelAdapter(this, roomList);
        recyclerViewRooms.setAdapter(adapter);

        // Fetch data from Firebase
        fetchRoomsForHotel(hotelId);
    }

    private void fetchRoomsForHotel(int hotelId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://hotelmobile-d180a-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("rooms");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                roomList.clear(); // Clear the old list before adding new data
                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    try {
                        // Parse Room object from snapshot
                        Room room = roomSnapshot.getValue(Room.class);
                        if (room != null && room.getHotel() != null
                                && room.getHotel().getHotelId() == hotelId) { // Lọc theo hotelId
                            roomList.add(room);
                        }
                    } catch (Exception e) {
                        Toast.makeText(RoomList.this, "Error parsing room data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                adapter.notifyDataSetChanged(); // Refresh adapter with new data
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RoomList.this, "Failed to load data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
