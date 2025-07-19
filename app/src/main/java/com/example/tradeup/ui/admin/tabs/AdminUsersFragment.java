// File: src/main/java/com/example/tradeup/ui/admin/tabs/AdminUsersFragment.java
package com.example.tradeup.ui.admin.tabs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tradeup.R;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentAdminUsersBinding;
import com.example.tradeup.ui.admin.AdminUserAdapter;
import com.example.tradeup.ui.admin.AdminViewModel;

public class AdminUsersFragment extends Fragment {

    private FragmentAdminUsersBinding binding;
    private AdminViewModel viewModel; // Dùng chung ViewModel với fragment cha
    private AdminUserAdapter adapter;
    private NavController navController;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(AdminViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        setupRecyclerView();
        setupSearchView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(user -> {
            // Khi nhấn vào user, điều hướng đến trang public profile của họ
            Bundle args = new Bundle();
            args.putString("userId", user.getUid());
            navController.navigate(R.id.action_global_to_publicProfileFragment, args);
        });
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewUsers.setAdapter(adapter);
    }

    private void setupSearchView() {
        binding.searchViewUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Hủy bỏ các tìm kiếm đang chờ và thực hiện ngay lập tức
                searchHandler.removeCallbacks(searchRunnable);
                viewModel.searchUsers(query);
                binding.searchViewUsers.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Sử dụng "debounce" để không gọi API mỗi khi gõ một ký tự
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> viewModel.searchUsers(newText);
                searchHandler.postDelayed(searchRunnable, 500); // Trì hoãn 500ms
                return true;
            }
        });
    }

    private void observeViewModel() {
        viewModel.isUserSearchLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarUsers.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getUserSearchResults().observe(getViewLifecycleOwner(), users -> {
            adapter.submitList(users);
            boolean hasResults = users != null && !users.isEmpty();
            boolean isQueryEmpty = binding.searchViewUsers.getQuery().toString().isEmpty();

            if (isQueryEmpty) {
                binding.textViewEmptyUsers.setText("Start typing to search for users.");
                binding.textViewEmptyUsers.setVisibility(View.VISIBLE);
            } else {
                binding.textViewEmptyUsers.setText("No users found.");
                binding.textViewEmptyUsers.setVisibility(hasResults ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}