// File: src/main/java/com/example/tradeup/ui/admin/ReportAdapter.java
package com.example.tradeup.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeup.data.model.Report;
import com.example.tradeup.databinding.ItemReportBinding;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ReportAdapter extends ListAdapter<Report, ReportAdapter.ReportViewHolder> {

    private final OnReportActionListener listener;
    private Map<String, String> reasonMap = Collections.emptyMap();

    public interface OnReportActionListener {
        void onViewContentClicked(Report report);
        void onResolveClicked(Report report);
    }

    public ReportAdapter(@NonNull OnReportActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportBinding binding = ItemReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReportViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        // Lời gọi này giờ đây sẽ khớp với định nghĩa hàm bind đã sửa
        holder.bind(getItem(position), reasonMap);
    }

    public void setReasonMap(Map<String, String> reasonMap) {
        this.reasonMap = (reasonMap != null) ? reasonMap : Collections.emptyMap();
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportBinding binding;
        private final OnReportActionListener listener;

        ReportViewHolder(ItemReportBinding binding, OnReportActionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        // *** SỬA LỖI Ở ĐÂY: Hàm bind phải nhận 2 tham số ***
        void bind(final Report report, final Map<String, String> reasonMap) {
            // Bind data
            binding.textViewReportType.setText(report.getReportedContentType().toUpperCase());

            // *** SỬA LỖI Ở ĐÂY: Sử dụng reasonMap để lấy tên đầy đủ ***
            String reasonName = reasonMap.getOrDefault(report.getReason(), report.getReason()); // Lấy tên, nếu không có thì dùng ID
            binding.textViewReason.setText(reasonName);

            binding.textViewDetails.setText(report.getDetails());

            // Cắt chuỗi ID để trông gọn gàng hơn
            String shortId = report.getReportingUserId().length() > 8
                    ? report.getReportingUserId().substring(0, 8) + "..."
                    : report.getReportingUserId();
            binding.textViewReportingUser.setText("Reported by: " + shortId);

            if (report.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                binding.textViewReportDate.setText(sdf.format(report.getCreatedAt().toDate()));
            }

            // Cập nhật text cho nút xem nội dung
            String viewContentText = "View Reported " + report.getReportedContentType();
            binding.textViewReportedContent.setText(viewContentText);

            // Set listeners
            binding.textViewReportedContent.setOnClickListener(v -> listener.onViewContentClicked(report));
            binding.buttonResolve.setOnClickListener(v -> listener.onResolveClicked(report));
        }
    }

    private static final DiffUtil.ItemCallback<Report> DIFF_CALLBACK = new DiffUtil.ItemCallback<Report>() {
        @Override
        public boolean areItemsTheSame(@NonNull Report oldItem, @NonNull Report newItem) {
            return oldItem.getReportId().equals(newItem.getReportId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Report oldItem, @NonNull Report newItem) {
            return Objects.equals(oldItem.getStatus(), newItem.getStatus());
        }
    };
}