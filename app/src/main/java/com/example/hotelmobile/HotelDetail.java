package com.example.hotelmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotelmobile.adapter.CommentAdapter;
import com.example.hotelmobile.model.Comment;
import com.example.hotelmobile.model.Hotel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public class HotelDetail extends AppCompatActivity {

    private ImageView imgHotelMain;
    private TextView tvHotelName, tvHotelLocation, tvHotelDescription;
    private LinearLayout layoutHotelImages, layoutSelectedImages;
    private ListView listComments;
    private EditText etNewComment;
    private Button btnChooseImages, btnSubmitComment, btnListRooms;
    private RatingBar ratingBar;
    private List<Comment> comments;
    private CommentAdapter commentAdapter;
    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> selectedImageUris = new ArrayList<>();

    private int hotelId; // ID của khách sạn (truyền từ Intent)
    private Hotel currentHotel; // Thông tin khách sạn hiện tại
    // Khởi tạo cấu hình Cloudinary
    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dbdd85bp4",
            "api_key", "947314781637449",
            "api_secret", "aEQ5nlEGafd_SBz7ZxK2QfcCzWQ"
    ));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        // Liên kết các thành phần giao diện
        imgHotelMain = findViewById(R.id.imgHotelMain);
        tvHotelName = findViewById(R.id.tvHotelName);
        tvHotelLocation = findViewById(R.id.tvHotelLocation);
        tvHotelDescription = findViewById(R.id.tvHotelDescription);
        layoutHotelImages = findViewById(R.id.layoutHotelImages);
        layoutSelectedImages = findViewById(R.id.layoutSelectedImages); // Layout để hiển thị ảnh chọn
        listComments = findViewById(R.id.listComments);
        etNewComment = findViewById(R.id.etNewComment);
        btnChooseImages = findViewById(R.id.btnChooseImages);
        btnSubmitComment = findViewById(R.id.btnSubmitComment);
        ratingBar = findViewById(R.id.ratingBar);

        btnListRooms = findViewById(R.id.btnListRooms);
        //Xem danh sách list room
        btnListRooms.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), RoomList.class);
            intent.putExtra("hotelId", hotelId); // selectedHotelId là ID của khách sạn
            startActivity(intent);
        });
        // Lấy hotelId từ Intent
        hotelId = getIntent().getIntExtra("hotel_id", -1);
        Log.d("HotelDetail", "Hotel ID: " + hotelId);

        if (hotelId == -1) {
            Toast.makeText(this, "Hotel ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải thông tin khách sạn
        loadHotelDetails();

        // Thiết lập adapter cho bình luận
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments);
        listComments.setAdapter(commentAdapter);

        // Tải danh sách bình luận

        loadComments();
        // Xử lý chọn ảnh
        btnChooseImages.setOnClickListener(v -> openImagePicker());

        // Xử lý thêm bình luận
        btnSubmitComment.setOnClickListener(v -> submitComment());

    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGE_REQUEST);
    }



    private void loadHotelDetails() {
        FirebaseDatabase.getInstance("https://hotelmobile-d180a-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("hotels")
                .child(String.valueOf(hotelId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentHotel = snapshot.getValue(Hotel.class);
                        if (currentHotel != null) {
                            // Hiển thị thông tin khách sạn
                            tvHotelName.setText(currentHotel.getHotelName());
                            tvHotelLocation.setText(currentHotel.getLocation());
                            tvHotelDescription.setText(currentHotel.getDescription());

                            // Hiển thị ảnh chính
                            Glide.with(HotelDetail.this)
                                    .load(currentHotel.getMainImg())
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(imgHotelMain);

                            // Hiển thị danh sách ảnh
                            if (currentHotel.getImages() != null) {
                                for (String imageUrl : currentHotel.getImages()) {
                                    ImageView imageView = new ImageView(HotelDetail.this);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 200);
                                    params.setMargins(8, 8, 8, 8);
                                    imageView.setLayoutParams(params);
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                    Glide.with(HotelDetail.this)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_launcher_background)
                                            .into(imageView);

                                    layoutHotelImages.addView(imageView);
                                }
                            }
                        } else {
                            Toast.makeText(HotelDetail.this, "Hotel not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HotelDetail.this, "Failed to load hotel details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadComments() {
        FirebaseDatabase.getInstance("https://hotelmobile-d180a-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("comments")
                .child(String.valueOf(hotelId))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        comments.clear();
                        for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                            Comment comment = commentSnapshot.getValue(Comment.class);
                            if (comment != null) {
                                comments.add(comment);
                            }
                        }
                        commentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HotelDetail.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitComment() {
        String commentText = etNewComment.getText().toString().trim();
        if (commentText.isEmpty() && selectedImageUris.isEmpty()) {
            Toast.makeText(this, "Please enter a comment or select images", Toast.LENGTH_SHORT).show();
            return;
        }
        float userRating = ratingBar.getRating();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "Nguời dùng");

        String commentId = FirebaseDatabase.getInstance("https://hotelmobile-d180a-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("comments")
                .child(String.valueOf(hotelId))
                .push()
                .getKey();

        List<String> uploadedImageUrls = new ArrayList<>();
        // Hiển thị thông báo chờ
        Toast.makeText(this, "Uploading images, please wait...", Toast.LENGTH_SHORT).show();

        // Upload ảnh trong một luồng riêng
        new Thread(() -> {
            try {
                for (Uri uri : selectedImageUris) {
                    // Đọc file bằng InputStream
                    try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                        // Upload ảnh lên Cloudinary
                        Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
                        String imageUrl = (String) uploadResult.get("secure_url");
                        uploadedImageUrls.add(imageUrl);
                    }
                }

                // Khi upload xong toàn bộ ảnh, tạo Comment và lưu vào Firebase
                runOnUiThread(() -> {
                    if (uploadedImageUrls.size() == selectedImageUris.size()) {
                        Comment newComment = new Comment(
                                commentId,
                                hotelId,
                                commentText,
                                System.currentTimeMillis(),
                                userName,
                                userRating,
                                uploadedImageUrls
                        );

                        FirebaseDatabase.getInstance("https://hotelmobile-d180a-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("comments")
                                .child(String.valueOf(hotelId))
                                .child(commentId)
                                .setValue(newComment)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                                    etNewComment.setText("");
                                    selectedImageUris.clear();
                                    layoutSelectedImages.removeAllViews();
                                    loadComments();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show());
                    }
                });
            } catch (IOException e) {
                Log.e("CloudinaryUpload", "Failed to upload images", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to upload images", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImageUris.add(imageUri);
            }

            // Hiển thị ảnh đã chọn trong LinearLayout
            layoutSelectedImages.removeAllViews();
            for (Uri uri : selectedImageUris) {
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 200);
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(this).load(uri).into(imageView);
                layoutSelectedImages.addView(imageView);
            }
        }
    }



}
