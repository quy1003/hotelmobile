package com.example.hotelmobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hotelmobile.adapter.HotelAdapter;
import com.example.hotelmobile.databaseHelper.HotelDBHelper;
import com.example.hotelmobile.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class ShortsFragment extends Fragment {
    private ListView listViewHotels;
    private HotelDBHelper hotelDBHelper;
    private List<Hotel> hotelList;
    private HotelAdapter hotelAdapter;

    // BroadcastReceiver để cập nhật danh sách khách sạn
    private final BroadcastReceiver hotelAddedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Gọi lại danh sách khách sạn khi nhận được broadcast
            loadHotels();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout của Fragment
        View rootView = inflater.inflate(R.layout.fragment_shorts, container, false);

        // Liên kết các thành phần giao diện
        listViewHotels = rootView.findViewById(R.id.listViewHotels);
        hotelDBHelper = new HotelDBHelper();
        hotelList = new ArrayList<>();

        // Thiết lập Adapter cho ListView
        hotelAdapter = new HotelAdapter(getContext(), hotelList);
        listViewHotels.setAdapter(hotelAdapter);

        // Tải dữ liệu khách sạn
        loadHotels();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Đăng ký BroadcastReceiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(hotelAddedReceiver, new IntentFilter("HOTEL_ADDED"));
        loadHotels(); // Gọi danh sách khách sạn khi Fragment bắt đầu
    }

    @Override
    public void onStop() {
        super.onStop();
        // Hủy đăng ký BroadcastReceiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(hotelAddedReceiver);
    }

    private void loadHotels() {
        hotelDBHelper.getAllHotels(new HotelDBHelper.DataStatus() {
            @Override
            public void onDataLoaded(List<Hotel> hotels) {
                hotelList.clear();
                hotelList.addAll(hotels); // Cập nhật danh sách khách sạn
                hotelAdapter.notifyDataSetChanged(); // Cập nhật giao diện
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
