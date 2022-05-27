package safetyfirst.androidapp.safetyfirstcontroller.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import safetyfirst.androidapp.safetyfirstcontroller.R;

public class LoginFragment extends Fragment {
    private EditText editTextMail, editTextPass;
    private Button logIn;
    private TextView register;

    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        setHasOptionsMenu(true);

        firebaseAuth = FirebaseAuth.getInstance();

        TextView register = rootView.findViewById(R.id.toRegister);
        Button logIn = rootView.findViewById(R.id.loginUser);

        EditText editTextMail = rootView.findViewById(R.id.loginEmail);
        EditText editTextPass = rootView.findViewById(R.id.loginPassword);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction addTransaction = getParentFragmentManager().beginTransaction();
                addTransaction.replace(R.id.fragment_container, new RegisterFragment());
                addTransaction.commit();
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextMail.getText().toString().trim();
                String password = editTextPass.getText().toString().trim();

                if (email.isEmpty()) {
                    editTextMail.setError("Email is required");
                    editTextMail.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextMail.setError("Enter a valid Email");
                    editTextMail.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    editTextPass.setError("Password is required");
                    editTextPass.requestFocus();
                    return;
                }
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    hideKeyboardFrom(requireContext(), view);

                                    NavigationView navigationView = (NavigationView) requireActivity().findViewById(R.id.nav_view);
                                    Menu nav_Menu = navigationView.getMenu();
                                    onPrepareOptionsMenu(nav_Menu);

                                    FragmentTransaction addTransaction = getParentFragmentManager().beginTransaction();
                                    addTransaction.replace(R.id.fragment_container, new HomeFragment());
                                    addTransaction.commit();

                                    Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            "Failed to login! Check credentials.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
        return rootView;
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager input = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        input.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
