package safetyfirst.androidapp.safetyfirstcontroller.fragments;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import safetyfirst.androidapp.safetyfirstcontroller.Model.User;
import safetyfirst.androidapp.safetyfirstcontroller.R;

public class RegisterFragment extends Fragment  {

    private TextView registerUser;
    private EditText editTextFullName, editTextAge, editTextEmail, editTextPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_register_user, container, false);

        mAuth = FirebaseAuth.getInstance();

        Button registerUser = rootView.findViewById(R.id.registerUser);

        EditText editTextFullName = rootView.findViewById(R.id.fullName);
        EditText editTextAge = rootView.findViewById(R.id.age);
        EditText editTextEmail = rootView.findViewById(R.id.email);
        EditText editTextPassword = rootView.findViewById(R.id.password);

        ProgressBar progressBar = rootView.findViewById(R.id.progressBar);

        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String fullName = editTextFullName.getText().toString().trim();
                String age = editTextAge.getText().toString().trim();

                if (fullName.isEmpty()) {
                    editTextFullName.setError("Full Name is required");
                    editTextFullName.requestFocus();
                    return;
                }
                if (age.isEmpty()) {
                    editTextAge.setError("Age is required");
                    editTextAge.requestFocus();
                    return;
                }
                if (email.isEmpty()) {
                    editTextEmail.setError("Email is required");
                    editTextEmail.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextEmail.setError("Valid Email is required");
                    editTextEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    editTextPassword.setError("Password is required");
                    editTextPassword.requestFocus();
                    return;
                }
                if (password.length() < 8) {
                    editTextPassword.setError("At least 8 characters");
                    editTextPassword.requestFocus();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    User user = new User(fullName, age, email);
                                    FirebaseDatabase.getInstance().getReference("Users")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(),
                                                                "Registered Successfully",
                                                                Toast.LENGTH_LONG).show();
                                                        progressBar.setVisibility(View.GONE);

                                                    } else {
                                                        Toast.makeText(getContext(),
                                                                "Failed to register",
                                                                Toast.LENGTH_LONG).show();
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(getContext(), "Failed to register", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
        return rootView;
    }
}

