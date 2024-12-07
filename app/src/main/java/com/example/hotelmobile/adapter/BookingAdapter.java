package com.example.hotelmobile.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelmobile.R;
import com.example.hotelmobile.model.Booking;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private final List<Booking> bookingList;

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.tvHotelName.setText(booking.getHotelName());
        holder.tvRoomName.setText(booking.getRoomName());
        holder.tvInvoiceCode.setText(booking.getInvoiceCode());
        holder.tvPaymentStatus.setText(booking.getPaymentStatus());
        holder.tvTotalPrice.setText(String.format("Price: %s", booking.getTotalPrice()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd", Locale.getDefault());

        if (booking.getStartDate() != null) {
            holder.tvStartDate.setText(String.format("Start: %s", dateFormat.format(booking.getStartDate())));
        }
        if (booking.getEndDate() != null) {
            holder.tvEndDate.setText(String.format("End: %s", dateFormat.format(booking.getEndDate())));
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvRoomName, tvInvoiceCode, tvPaymentStatus, tvTotalPrice, tvStartDate, tvEndDate;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvInvoiceCode = itemView.findViewById(R.id.tvInvoiceCode);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
        }
    }
}
