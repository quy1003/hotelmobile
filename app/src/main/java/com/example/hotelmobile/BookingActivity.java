package com.example.hotelmobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelmobile.Api.CreateOrder;
import com.example.hotelmobile.model.Booking;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class BookingActivity extends AppCompatActivity {

    private TextView tvHotelName, tvRoomName, tvStartDate, tvEndDate, tvTotalPrice, tvPaymentStatus;
    private Button btnStartDate, btnEndDate, btnBookRoom;
    private long pricePerNight;
    private String roomName, hotelName;
    private Calendar startDateCalendar, endDateCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private static final String PREFERENCE_NAME = "com.example.hotelmobile.PREFERENCES";
    private static final String HOTEL_NAME_KEY = "hotel_name_key";
    private static final String ROOM_NAME_KEY = "room_name_key";
    private static final String PRICE_KEY = "price_key";
    double totalBill = 0;
    private List<Date> bookedDates = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Initialize Views
        tvHotelName = findViewById(R.id.tvHotelName);
        tvRoomName = findViewById(R.id.tvRoomName);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);

        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnBookRoom = findViewById(R.id.btnBookRoom);
        //ZaloPay Init
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);

        //
        // Get room details from SharedPreferences
        getRoomDetailsFromPreferences();
        fetchBookedDates(roomName);
        // Set hotel and room name from SharedPreferences
        tvHotelName.setText(hotelName);
        tvRoomName.setText(roomName);

        // Initialize Calendar instances for start and end dates
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        // Thiết lập startDate là ngày mai
//        startDateCalendar.add(Calendar.DAY_OF_YEAR, 1);

        // Thiết lập endDate là 2 ngày nữa
//        endDateCalendar.add(Calendar.DAY_OF_YEAR, 2);

        // Set default date values
//        tvStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
//        tvEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
        tvStartDate.setText("Select the date");
        tvEndDate.setText("Select the date");
        // Set onClickListener for Start Date button
        btnStartDate.setOnClickListener(v -> showDatePickerDialog(true));

        // Set onClickListener for End Date button
        btnEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Set onClickListener for Booking button
        btnBookRoom.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance(); // Lấy ngày hiện tại
            //Check date
            boolean hasConflict = false;
            for (Date bookedDate : bookedDates) {
                if (!bookedDate.before(startDateCalendar.getTime()) && !bookedDate.after(endDateCalendar.getTime())) {
                    hasConflict = true;
                    break;
                }
            }
            //


            if (startDateCalendar.before(today)) {
                Toast.makeText(BookingActivity.this, "Start Date must not be before today, start date must be tomorrow", Toast.LENGTH_SHORT).show();
            }
            else if(hasConflict){
                Toast.makeText(BookingActivity.this, "The selected date range conflicts with existing bookings.", Toast.LENGTH_SHORT).show();
                return;

            }
            else if (startDateCalendar.before(endDateCalendar)) {
                // Tích hợp thanh toán
                CreateOrder orderApi = new CreateOrder();
                try {
                    JSONObject data = orderApi.createOrder(String.valueOf(tvTotalPrice.getText().toString()));
                    Log.d("Amount", tvTotalPrice.getText().toString());
                    String code = data.getString("return_code");
                    if (code.equals("1")) {
                        String token = data.getString("zp_trans_token");
                        ZaloPaySDK.getInstance().payOrder(BookingActivity.this, token, "demozpdk://app", new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(String s, String s1, String s2) {
                                saveInvoiceToFirebase();
                                Intent intent = new Intent(BookingActivity.this, PaymentNotification.class);
                                intent.putExtra("result", "successfully!");

                                startActivity(intent);
                            }

                            @Override
                            public void onPaymentCanceled(String s, String s1) {
                                Intent intent = new Intent(BookingActivity.this, PaymentNotification.class);
                                intent.putExtra("result", "But it was cancelled !");
                                startActivity(intent);
                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                                Intent intent = new Intent(BookingActivity.this, PaymentNotification.class);
                                intent.putExtra("result", "But it was failed :( !");
                                startActivity(intent);
                            }
                        });
                    }
                    else{
                        Toast.makeText(BookingActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(BookingActivity.this, "End Date must be after Start Date", Toast.LENGTH_SHORT).show();
            }




        });
    }

    private void saveInvoiceToFirebase() {
        // Lấy thông tin userId từ SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = String.valueOf(preferences.getInt("user_id", -1)); // Default -1 nếu không tìm thấy

        // Tạo mã hóa đơn (invoiceCode)
        String invoiceCode = "INV" + System.currentTimeMillis();

        // Lấy thông tin từ giao diện
        String hotelName = tvHotelName.getText().toString();
        String roomName = tvRoomName.getText().toString();
        Date startDate = startDateCalendar.getTime(); // Dữ liệu vẫn giữ kiểu Date
        Date endDate = endDateCalendar.getTime();     // Dữ liệu vẫn giữ kiểu Date
        double totalPrice = Double.parseDouble(tvTotalPrice.getText().toString().replace("Total Price: ", "").replace(" VND", ""));
        String paymentStatus = "Successful";

        // Định dạng ngày thành chuỗi ISO (cho Firebase lưu)
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        String formattedStartDate = isoFormat.format(startDate);
        String formattedEndDate = isoFormat.format(endDate);

        // Tạo đối tượng Booking (với kiểu Date cho startDate và endDate)
        Booking invoice = new Booking(hotelName, roomName, invoiceCode, startDate, endDate, totalPrice, paymentStatus, userId);

        // Chuyển đổi dữ liệu thành Map để lưu các trường ISO string
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("hotelName", hotelName);
        bookingData.put("roomName", roomName);
        bookingData.put("invoiceCode", invoiceCode);
        bookingData.put("startDate", formattedStartDate); // Lưu chuỗi ISO
        bookingData.put("endDate", formattedEndDate);     // Lưu chuỗi ISO
        bookingData.put("totalPrice", totalPrice);
        bookingData.put("paymentStatus", paymentStatus);
        bookingData.put("userId", userId);

        // Lưu vào Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://hotelmobile-d180a-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("bookings");

        databaseReference.child(invoiceCode).setValue(bookingData)
                .addOnSuccessListener(aVoid -> Toast.makeText(BookingActivity.this, "Invoice saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(BookingActivity.this, "Failed to save invoice: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchBookedDates(String roomName) {
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance("https://hotelmobile-d180a-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("bookings");

        bookingsRef.orderByChild("roomName").equalTo(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    try {
                        // Lấy startDate và endDate từ Firebase
                        String startDateStr = bookingSnapshot.child("startDate").getValue(String.class);
                        String endDateStr = bookingSnapshot.child("endDate").getValue(String.class);

                        Date startDate = isoFormat.parse(startDateStr);
                        Date endDate = isoFormat.parse(endDateStr);

                        // Thêm tất cả các ngày giữa startDate và endDate vào danh sách
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(startDate);
                        while (!calendar.getTime().after(endDate)) {
                            bookedDates.add(calendar.getTime());
                            calendar.add(Calendar.DAY_OF_YEAR, 1); // Tăng ngày lên 1
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingActivity.this, "Error fetching booked dates: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Retrieve room details from SharedPreferences
    private void getRoomDetailsFromPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        hotelName = preferences.getString(HOTEL_NAME_KEY, "Unknown Hotel");
        roomName = preferences.getString(ROOM_NAME_KEY, "Unknown Room");
        pricePerNight = preferences.getLong(PRICE_KEY, 0);
        tvTotalPrice.setText(String.valueOf(pricePerNight));
    }

    // Show date picker dialog for start date or end date
    private void showDatePickerDialog(boolean isStartDate) {
        if (bookedDates == null || bookedDates.isEmpty()) {
            Toast.makeText(this, "Please wait while booked dates are being loaded...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển bookedDates thành danh sách timestamp và chuẩn hóa
        List<Long> disabledTimestamps = new ArrayList<>();
        for (Date bookedDate : bookedDates) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(bookedDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            disabledTimestamps.add(calendar.getTimeInMillis());
            Log.d("Quý check: ", calendar.getTime().toString());
        }

        // Sắp xếp danh sách ngày bị vô hiệu hóa
        Collections.sort(disabledTimestamps);

        // Tạo validator tùy chỉnh
        CalendarConstraints.DateValidator validator = new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                // Chuẩn hóa timestamp về đầu ngày (GMT+0)
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(date);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long normalizedDate = calendar.getTimeInMillis();

                // Kiểm tra nếu ngày nằm trong danh sách vô hiệu hóa
                return !disabledTimestamps.contains(normalizedDate);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                // Không cần ghi thông tin vào parcel
            }
        };

        // Thiết lập CalendarConstraints với validator
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(validator);

        // Khởi tạo DatePicker với ràng buộc
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(isStartDate ? "Select Start Date" : "Select End Date");
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Long> datePicker = builder.build();

        // Xử lý khi người dùng chọn ngày
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);

            // Chuẩn hóa ngày được chọn
            selectedDate.set(Calendar.HOUR_OF_DAY, 0);
            selectedDate.set(Calendar.MINUTE, 0);
            selectedDate.set(Calendar.SECOND, 0);
            selectedDate.set(Calendar.MILLISECOND, 0);
            long normalizedDate = selectedDate.getTimeInMillis();

//            // Kiểm tra nếu ngày được chọn liền kề với ngày đã đặt
//            for (int i = 0; i < disabledTimestamps.size(); i++) {
//                long disabledDate = disabledTimestamps.get(i);
//
//                // Kiểm tra nếu ngày được chọn liền kề hoặc tạo khoảng không hợp lệ
//                if (Math.abs(normalizedDate - disabledDate) <= 24 * 60 * 60 * 1000) {
//                    Toast.makeText(this, "Please choose a valid date range.", Toast.LENGTH_SHORT).show();
//                    return; // Dừng xử lý khi ngày không hợp lệ
//                }
//            }

            if (isStartDate) {
                startDateCalendar.setTime(selectedDate.getTime());
                tvStartDate.setText(dateFormat.format(selectedDate.getTime()));
            } else {
                endDateCalendar.setTime(selectedDate.getTime());
                tvEndDate.setText(dateFormat.format(selectedDate.getTime()));
            }
            calculateTotalPrice(); // Cập nhật tổng tiền khi ngày được thay đổi
        });

        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }


    // Calculate the total price based on the start date, end date, and price per night
    private void calculateTotalPrice() {
        long startDateInMillis = startDateCalendar.getTimeInMillis();
        long endDateInMillis = endDateCalendar.getTimeInMillis();

        long diffInMillis = endDateInMillis - startDateInMillis;
        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);  // Convert milliseconds to days

        if (diffInDays <= 0) {
//            Toast.makeText(BookingActivity.this, "The duration between start and end date must be positive", Toast.LENGTH_SHORT).show();
        } else {
            long totalPrice = pricePerNight * diffInDays;
            totalBill = totalPrice;
            tvTotalPrice.setText(String.valueOf(totalPrice));
            tvPaymentStatus.setText("Payment Status: Pending");
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

}
