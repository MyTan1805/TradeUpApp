package com.example.tradeup.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeup.R;
import com.example.tradeup.databinding.FragmentBottomSheetSelectionBinding; // Dùng ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ListSelectionDialogFragment extends BottomSheetDialogFragment {

    public static final String RESULT_SELECTED_INDEX = "selected_index";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_ITEMS = "arg_items";
    private static final String ARG_REQUEST_KEY = "arg_request_key";

    private FragmentBottomSheetSelectionBinding binding;
    private String title;
    private ArrayList<String> items;
    private String requestKey;

    public static ListSelectionDialogFragment newInstance(String title, ArrayList<String> items, String requestKey) {
        ListSelectionDialogFragment fragment = new ListSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putStringArrayList(ARG_ITEMS, items);
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            items = getArguments().getStringArrayList(ARG_ITEMS);
            requestKey = getArguments().getString(ARG_REQUEST_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBottomSheetSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.textViewDialogTitle.setText(title);

        SelectionAdapter adapter = new SelectionAdapter(items, position -> {
            Bundle result = new Bundle();
            result.putInt(RESULT_SELECTED_INDEX, position);
            getParentFragmentManager().setFragmentResult(requestKey, result);
            dismiss();
        });

        binding.recyclerViewSelection.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Adapter cho RecyclerView ---
    private static class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.SelectionViewHolder> {
        private final List<String> items;
        private final OnItemSelectedListener listener;

        interface OnItemSelectedListener {
            void onItemSelected(int position);
        }

        SelectionAdapter(List<String> items, OnItemSelectedListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public SelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_sheet_selection, parent, false);
            return new SelectionViewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull SelectionViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class SelectionViewHolder extends RecyclerView.ViewHolder {
            TextView textViewOption;
            OnItemSelectedListener listener;

            SelectionViewHolder(@NonNull View itemView, OnItemSelectedListener listener) {
                super(itemView);
                this.listener = listener;
                textViewOption = itemView.findViewById(R.id.textViewOption);
                itemView.setOnClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onItemSelected(getAdapterPosition());
                    }
                });
            }

            void bind(String text) {
                textViewOption.setText(text);
            }
        }
    }
}