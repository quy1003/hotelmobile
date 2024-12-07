package com.example.hotelmobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelmobile.RoomDetailActivity;
import com.example.hotelmobile.model.Room;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hotelmobile.R;

public class RoomInHotelAdapter extends RecyclerView.Adapter<RoomInHotelAdapter.RoomViewHolder> {
    private Context context;
    private List<Room> roomList;

    // SharedPreferences keys
    private static final String PREFERENCE_NAME = "com.example.hotelmobile.PREFERENCES";
    private static final String ROOM_ID_KEY = "room_id_key";

    public RoomInHotelAdapter(Context context, List<Room> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.room_item, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        // Set room number
        holder.tvRoomNumber.setText("Room Number: " + room.getRoomNumber());

        // Set price per night
        holder.tvRoomPrice.setText("Price: $" + room.getPricePerNight() + "/night");

        // Set availability
        holder.tvRoomAvailability.setText(room.isAvailable() ? "Available" : "Booked");
        holder.tvRoomAvailability.setTextColor(room.isAvailable()
                ? ContextCompat.getColor(context, R.color.green)
                : ContextCompat.getColor(context, R.color.red));

        // Set hotel name
        if (room.getHotel() != null) {
            holder.tvHotelName.setText("Hotel: " + room.getHotel().getHotelName());
        } else {
            holder.tvHotelName.setText("Hotel: N/A");
        }

        // Set category name
        if (room.getCategory() != null) {
            holder.tvRoomCategory.setText("Category: " + room.getCategory().getName());
        } else {
            holder.tvRoomCategory.setText("Category: N/A");
        }

        // Load main image
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            Glide.with(context)
                    .load(room.getImages().get(0)) // Load first image
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgRoomMain);
        } else {
            holder.imgRoomMain.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            // Save room ID to SharedPreferences
            saveRoomIdToPreferences(room.getRoomId());

            // Start RoomDetailActivity
            Intent intent = new Intent(context, RoomDetailActivity.class);
            intent.putExtra("room_id", room.getRoomId()); // (Optional) Add room_id for verification
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRoomMain;
        TextView tvRoomNumber, tvRoomPrice, tvRoomAvailability, tvHotelName, tvRoomCategory;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoomMain = itemView.findViewById(R.id.imgRoomMain);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomAvailability = itemView.findViewById(R.id.tvRoomAvailability);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvRoomCategory = itemView.findViewById(R.id.tvRoomCategory);
        }
    }

    // Save room ID to SharedPreferences
    private void saveRoomIdToPreferences(int roomId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ROOM_ID_KEY, roomId);
        editor.apply();
    }
}
