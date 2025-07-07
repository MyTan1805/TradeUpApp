// File: src/main/java/com/example/tradeup/ui/adapters/AddressResultAdapter.java
package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeup.data.model.nominatim.GeocodingResult;
import com.example.tradeup.databinding.ItemAddressResultBinding;

public class AddressResultAdapter extends ListAdapter<GeocodingResult, AddressResultAdapter.ViewHolder> {

    private final OnAddressClickListener listener;

    public interface OnAddressClickListener {
        void onAddressClick(GeocodingResult address);
    }

    public AddressResultAdapter(OnAddressClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressResultBinding binding = ItemAddressResultBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddressResultBinding binding;

        ViewHolder(ItemAddressResultBinding binding, OnAddressClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAddressClick(getItem(position));
                }
            });
        }

        void bind(GeocodingResult address) {
            binding.textViewAddress.setText(address.displayName);
        }
    }

    private static final DiffUtil.ItemCallback<GeocodingResult> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GeocodingResult>() {
                @Override
                public boolean areItemsTheSame(@NonNull GeocodingResult oldItem, @NonNull GeocodingResult newItem) {
                    return oldItem.placeId == newItem.placeId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull GeocodingResult oldItem, @NonNull GeocodingResult newItem) {
                    return oldItem.displayName.equals(newItem.displayName);
                }
            };
}