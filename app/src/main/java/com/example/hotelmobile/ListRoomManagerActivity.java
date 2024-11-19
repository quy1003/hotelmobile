package com.example.hotelmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelmobile.adapter.RoomAdapter;
import com.example.hotelmobile.databaseHelper.HotelDBHelper;
import com.example.hotelmobile.databaseHelper.RoomDBHelper;
import com.example.hotelmobile.model.Hotel;
import com.example.hotelmobile.model.Room;

import java.util.ArrayList;
import java.util.List;

public class ListRoomManagerActivity extends AppCompatActivity {
    private Spinner spinnerHotels;
    private ListView listViewRooms;
    private Button btnAddRoom;
    private HotelDBHelper hotelDBHelper;
    private RoomDBHelper roomDBHelper;
    private List<Hotel> hotelList;
    private List<Room> roomList;
    private RoomAdapter roomAdapter;
    private ArrayAdapter<String> hotelSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_room_manager);

        spinnerHotels = findViewById(R.id.spinnerHotels);
        listViewRooms = findViewById(R.id.listViewRooms);
        btnAddRoom = findViewById(R.id.btnAddRoom);

        hotelDBHelper = new HotelDBHelper();
        roomDBHelper = new RoomDBHelper();
        hotelList = new ArrayList<>();
        roomList = new ArrayList<>();

        // Cấu hình Adapter cho Spinner
        hotelSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        hotelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHotels.setAdapter(hotelSpinnerAdapter);

        // Cấu hình Adapter cho ListView Room
        roomAdapter = new RoomAdapter(this, roomList);
        listViewRooms.setAdapter(roomAdapter);

        // Tải danh sách khách sạn và cấu hình Spinner
        loadHotels();

        // Xử lý sự kiện khi chọn một khách sạn trong Spinner
        spinnerHotels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < hotelList.size()) {
                    Hotel selectedHotel = hotelList.get(position);
                    loadRooms(selectedHotel.getHotelId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                roomList.clear();
                roomAdapter.notifyDataSetChanged();
            }
        });

        // Xử lý sự kiện khi nhấn nút Add New Room
        btnAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(ListRoomManagerActivity.this, AddRoomActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tự động tải lại danh sách phòng khi quay lại từ AddRoomActivity
        int selectedPosition = spinnerHotels.getSelectedItemPosition();
        if (selectedPosition >= 0 && selectedPosition < hotelList.size()) {
            Hotel selectedHotel = hotelList.get(selectedPosition);
            loadRooms(selectedHotel.getHotelId());
        }
    }

    private void loadHotels() {
        hotelDBHelper.getAllHotels(new HotelDBHelper.DataStatus() {
            @Override
            public void onDataLoaded(List<Hotel> hotels) {
                hotelList.clear();
                hotelList.addAll(hotels);

                // Cập nhật dữ liệu cho Spinner
                List<String> hotelNames = new ArrayList<>();
                for (Hotel hotel : hotels) {
                    hotelNames.add(hotel.getHotelName());
                }
                hotelSpinnerAdapter.clear();
                hotelSpinnerAdapter.addAll(hotelNames);
                hotelSpinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ListRoomManagerActivity.this, "Error loading hotels: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRooms(int hotelId) {
        roomDBHelper.getAllRooms(new RoomDBHelper.DataStatus() {
            @Override
            public void onDataLoaded(List<Room> rooms) {
                roomList.clear();
                for (Room room : rooms) {
                    if (room.getHotelID() == hotelId) {
                        roomList.add(room);
                    }
                }
                roomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ListRoomManagerActivity.this, "Error loading rooms: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
