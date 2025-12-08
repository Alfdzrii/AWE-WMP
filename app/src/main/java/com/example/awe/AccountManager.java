
package com.example.awe;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

// Kelas untuk merepresentasikan satu akun yang disimpan
class SavedAccount {
    private String uid;
    private String email;
    private String username; // Bisa ditambahkan nanti

    public SavedAccount(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    // Getters
    public String getUid() { return uid; }
    public String getEmail() { return email; }
}

/**
 * Kelas helper untuk mengelola penyimpanan dan pengambilan daftar akun
 * dari SharedPreferences menggunakan format JSON (dengan Gson).
 */
public class AccountManager {

    private static final String SHARED_PREFS_NAME = "saved_accounts_prefs";
    private static final String ACCOUNTS_LIST_KEY = "accounts_list";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public AccountManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Menyimpan atau memperbarui akun dalam daftar.
     */
    public void saveOrUpdateAccount(SavedAccount newAccount) {
        List<SavedAccount> accounts = getSavedAccounts();

        boolean accountExists = false;
        for (int i = 0; i < accounts.size(); i++) {
            // === PERUBAHAN DI SINI ===
            // Ganti getUid() menjadi getEmail() agar tidak ada duplikat email
            if (accounts.get(i).getEmail().equals(newAccount.getEmail())) {
                accounts.set(i, newAccount); // Update akun yang sudah ada (termasuk UID baru jika ada)
                accountExists = true;
                break;
            }
        }

        if (!accountExists) {
            accounts.add(newAccount); // Tambah akun baru jika email belum ada
        }

        saveAccountsList(accounts);
    }

    /**
     * Mengambil daftar semua akun yang tersimpan.
     */
    public List<SavedAccount> getSavedAccounts() {
        String json = sharedPreferences.getString(ACCOUNTS_LIST_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<SavedAccount>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Menghapus akun dari daftar berdasarkan UID.
     */
    public void removeAccount(String uid) {
        List<SavedAccount> accounts = getSavedAccounts();
        accounts.removeIf(account -> account.getUid().equals(uid));
        saveAccountsList(accounts);
    }

    /**
     * Menyimpan daftar akun yang sudah diperbarui ke SharedPreferences.
     */
    private void saveAccountsList(List<SavedAccount> accounts) {
        String json = gson.toJson(accounts);
        sharedPreferences.edit().putString(ACCOUNTS_LIST_KEY, json).apply();
    }
    
    public void clearAllAccounts() {
        sharedPreferences.edit().remove(ACCOUNTS_LIST_KEY).apply();
    }
}
