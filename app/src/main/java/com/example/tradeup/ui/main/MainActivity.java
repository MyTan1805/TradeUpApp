// File: src/main/java/com/example/tradeup/ui/main/MainActivity.java
package com.example.tradeup.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination; // Có thể không cần nhưng nên có
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.tradeup.R;
import com.example.tradeup.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Lấy NavController một cách an toàn
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = Objects.requireNonNull(navHostFragment).getNavController();

        // 2. Tự động kết nối BottomNavigationView với NavController
        // Dòng này sẽ xử lý việc chuyển fragment và highlight item được chọn
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);

        // 3. Xử lý sự kiện nhấn vào nút FAB
        binding.fabAddItem.setOnClickListener(v -> {
            // Đảm bảo ID này tồn tại trong main_nav.xml
            navController.navigate(R.id.addItemFragment);
        });

        // 4. Vô hiệu hóa việc nhấn vào item giữ chỗ để tránh các hành vi lạ
        binding.bottomNavigationView.findViewById(R.id.navigation_placeholder).setClickable(false);

        // 5. Thiết lập logic ẩn/hiện thanh điều hướng
        setupBottomBarVisibility();
    }

    private void setupBottomBarVisibility() {
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // "Chiêu" tự kiểm tra: Hỏi BottomNavigationView xem màn hình hiện tại
            // có tương ứng với một item nào trong menu của nó không.
            boolean shouldShow = binding.bottomNavigationView.getMenu().findItem(destination.getId()) != null;

            toggleBottomBar(shouldShow);
        });
    }

    /**
     * Hàm duy nhất để xử lý việc hiện hoặc ẩn BottomAppBar và FAB một cách mượt mà.
     * @param show true để hiện, false để ẩn.
     */
    private void toggleBottomBar(boolean show) {
        if (show) {
            binding.bottomAppBar.setVisibility(View.VISIBLE);
            binding.fabAddItem.setVisibility(View.VISIBLE);
        } else {
            binding.bottomAppBar.setVisibility(View.GONE);
            binding.fabAddItem.setVisibility(View.GONE);
        }
    }
}