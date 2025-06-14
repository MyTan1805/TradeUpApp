package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Tasks; // Cho Tasks.forException
import javax.inject.Inject;

public class FirebaseAuthSource {
    private final FirebaseAuth firebaseAuth;

    @Inject
    public FirebaseAuthSource(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Task<AuthResult> registerUser(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginUser(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public void logoutUser() {
        firebaseAuth.signOut();
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    public Task<Void> sendEmailVerification(@NonNull FirebaseUser user) {
        // FirebaseUser.sendEmailVerification() đã trả về Task<Void>
        return user.sendEmailVerification();
    }
}