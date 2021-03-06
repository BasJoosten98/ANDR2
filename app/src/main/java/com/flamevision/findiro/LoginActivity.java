package com.flamevision.findiro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.flamevision.findiro.LoginAndSignup.CustomLoginActivity;
import com.flamevision.findiro.LoginAndSignup.CustomSignupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = CustomLoginActivity.class.getName();

    private EditText etEmail;
    private EditText etPass;
    private Button btnLoginEmail;
    private Button signup;

    private FirebaseAuth firebaseAuth;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.EmailValue);
        etPass = findViewById(R.id.PassValue);
        btnLoginEmail = findViewById(R.id.btnSignValue);

        // [START initialize_fblogin]
        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();


        btnLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginViaEmail();
            }
        });

        signup = findViewById(R.id.btnSignUpValue);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CustomSignupActivity.class);
                startActivity(intent);
            }
        });

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            //USER IS LOGGED IN
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    }
    // [END on_start_check_user]

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookAccessToken:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }
    // [END auth_with_facebook]

    private void loginViaEmail(){
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        Log.d("Login", "Entered email: " + email + ", pass: " + pass);

        boolean error = false;
        if (email.equalsIgnoreCase("")) {
            etEmail.setError("You must enter your email.");
            error = true;
        }
        if (pass.equalsIgnoreCase("")) {
            etPass.setError("You must enter your password.");
            error = true;
        }

        if(error){
            Log.d("Login", "EditText error occurred");
            Toast.makeText(LoginActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
        }
        else  {
            Log.d("Login", "Trying to login");
            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //login success
                        Log.d("Login", "Login success");
                        setResult(RESULT_OK);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else {
                        //login failed
                        Log.d("Login", "Login failed due to exception: " + task.getException());
                        if(task.getException() instanceof FirebaseAuthInvalidUserException){
                            //Email does not exists, show sign up screen
                            etEmail.setError("No account exists with this email.");
                        }
                        else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                            //email does exists, but wrong password
                            etPass.setError("Wrong password.");
                        }
                        Toast.makeText(LoginActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // [START on_activity_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    // [END on_activity_result]
}