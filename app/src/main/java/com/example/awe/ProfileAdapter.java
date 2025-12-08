
package com.example.awe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private final List<SavedAccount> accountList;
    private final OnProfileInteractionListener listener;

    // Interface untuk klik profil
    public interface OnProfileInteractionListener {
        void onProfileClicked(SavedAccount account);
        void onRemoveClicked(SavedAccount account, int position);
    }

    public ProfileAdapter(List<SavedAccount> accountList, OnProfileInteractionListener listener) {
        this.accountList = accountList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        SavedAccount currentAccount = accountList.get(position);
        holder.emailTextView.setText(currentAccount.getEmail());

        // klik pada seluruh item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileClicked(currentAccount);
            }
        });

        //  klik pada tombol hapus
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClicked(currentAccount, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    // ViewHolder untuk satu item profil
    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        ImageButton removeButton;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.profile_email);
            removeButton = itemView.findViewById(R.id.remove_account_button);

        }
    }

}

