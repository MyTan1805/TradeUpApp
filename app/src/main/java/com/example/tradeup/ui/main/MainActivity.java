package com.example.tradeup.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI; // Dùng cho setupWithNavController

// Import lớp Binding (tên sẽ phụ thuộc vào tên file layout activity_main.xml)
// Ví dụ: nếu layout là activity_main.xml -> ActivityMainBinding
import com.example.tradeup.R;
import com.example.tradeup.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // Khai báo biến binding
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Thiết lập BottomNavigationView với NavController
            // Sử dụng ID của BottomNavigationView từ layout của bạn
            BottomNavigationView bottomNavigationView = binding.bottomNavigationView; // Hoặc findViewById(R.id.your_bottom_nav_id) nếu không dùng binding ở đây
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // Lắng nghe sự kiện thay đổi đích đến
            navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
                @Override
                public void onDestinationChanged(@NonNull NavController controller,
                                                 @NonNull NavDestination destination,
                                                 @Nullable Bundle arguments) {
                    BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
                    FloatingActionButton fabPostItem = binding.fabPostItem;
                    int destinationId = destination.getId(); // Lấy ID một lần

                    if (destinationId == R.id.navigation_home ||
                            destinationId == R.id.navigation_messages ||
                            destinationId == R.id.navigation_notifications ||
                            destinationId == R.id.navigation_profile) {

                        bottomNavigationView.setVisibility(View.VISIBLE);
                        if (fabPostItem != null) {
                            fabPostItem.setVisibility(View.VISIBLE);
                        }
                    } else {
                        bottomNavigationView.setVisibility(View.GONE);
                        if (fabPostItem != null) {
                            fabPostItem.setVisibility(View.GONE);
                        }
                    }
                }
            });

            // Xử lý click cho FloatingActionButton (nếu có)
            FloatingActionButton fabPostItem = binding.fabPostItem; // Hoặc findViewById(R.id.your_fab_id)
            if (fabPostItem != null) {
                fabPostItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: Điều hướng đến màn hình Đăng tin (AddItemFragment)
                        // Ví dụ: navController.navigate(R.id.addItemFragment);
                        // Đảm bảo ID addItemFragment đúng và đã được định nghĩa trong nav_graph
                        try {
                            navController.navigate(R.id.addItemFragment);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Không thể mở màn hình đăng tin.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } else {
            // Xử lý trường hợp navHostFragment là null nếu cần thiết
            // (Điều này không nên xảy ra nếu layout của bạn được thiết lập đúng)
        }
    }

    // (Tùy chọn) Override để hỗ trợ nút back của Toolbar nếu bạn có Toolbar và thiết lập nó với NavController
    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}