// Tạo package mới: ui.admin.tabs
// Tạo file mới: src/main/java/com/example/tradeup/ui/admin/tabs/AdminReportsFragment.java
package com.example.tradeup.ui.admin.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Report;
import com.example.tradeup.databinding.FragmentAdminReportsBinding;
import com.example.tradeup.ui.admin.AdminViewModel;
import com.example.tradeup.ui.admin.ReportAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminReportsFragment extends Fragment {

    private FragmentAdminReportsBinding binding;
    private AdminViewModel viewModel; // Sẽ dùng chung ViewModel với fragment cha
    private ReportAdapter adapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel từ fragment cha (AdminDashboardFragment)
        viewModel = new ViewModelProvider(requireParentFragment()).get(AdminViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ReportAdapter(new ReportAdapter.OnReportActionListener() {
            @Override
            public void onViewContentClicked(Report report) {
                viewModel.viewReportedContent(report);
            }

            @Override
            public void onResolveClicked(Report report) {
                showResolveActionsDialog(report);
            }
        });
        binding.recyclerViewReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewReports.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getReports().observe(getViewLifecycleOwner(), reports -> {
            adapter.submitList(reports);
            binding.textViewEmpty.setVisibility(reports == null || reports.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getReasonMap().observe(getViewLifecycleOwner(), reasonMap -> {
            if (adapter != null) {
                adapter.setReasonMap(reasonMap);
            }
        });

        // Listener điều hướng vẫn được xử lý ở fragment cha (AdminDashboardFragment)
    }

    private void showResolveActionsDialog(Report report) {
        final CharSequence[] actions = {"Delete Content", "Suspend User", "Dismiss Report (No Action)", "Cancel"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Resolve Report")
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: viewModel.deleteContent(report); break;
                        case 1: viewModel.suspendUserAccount(report); break;
                        case 2: viewModel.dismissReport(report); break;
                        case 3: dialog.dismiss(); break;
                    }
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}