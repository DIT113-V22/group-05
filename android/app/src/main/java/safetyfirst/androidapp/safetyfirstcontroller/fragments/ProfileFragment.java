package safetyfirst.androidapp.safetyfirstcontroller.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import safetyfirst.androidapp.safetyfirstcontroller.Model.User;
import safetyfirst.androidapp.safetyfirstcontroller.R;

public class ProfileFragment extends Fragment {
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance("https://auth-75895-default-rtdb.europe-west1.firebasedatabase.app/").getReference()
                .child("Users");
        userID = user.getUid();

        final TextView greeting = (TextView) rootView.findViewById(R.id.greet);
        final TextView fullNameG = (TextView) rootView.findViewById(R.id.fullName);
        final TextView emailG = (TextView) rootView.findViewById(R.id.email);
        final TextView ageG = (TextView) rootView.findViewById(R.id.age);

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {

                    String fullName = user.fullName;
                    String email = user.email;
                    String age = user.age;

                    greeting.setText("Welcome, " + fullName + "!");
                    fullNameG.setText(fullName);
                    emailG.setText(email);
                    ageG.setText(age);
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){
                Toast.makeText(getContext(), "Something went wrong :(", Toast.LENGTH_LONG).show();
            }
        });
        return rootView;
    }
}





