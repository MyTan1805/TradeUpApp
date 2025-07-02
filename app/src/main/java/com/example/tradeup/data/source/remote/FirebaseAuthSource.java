package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Tasks; // Cho Tasks.forException
import com.google.firebase.auth.GoogleAuthProvider;

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

    public Task<AuthResult> loginWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return firebaseAuth.signInWithCredential(credential);
    }

    public Task<Void> reauthenticateAndDeleteCurrentUser(String password) {
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            return Tasks.forException(new Exception("User is not logged in or has no email to re-authenticate."));
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);


        return user.reauthenticate(credential).onSuccessTask(aVoid -> {
            return user.delete();
        });
    }

}