package safetyfirst.androidapp.safetyfirstcontroller.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import safetyfirst.androidapp.safetyfirstcontroller.R;

public class ContactsFragment extends Fragment implements View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        Button addContact = rootView.findViewById(R.id.add_contact);
        addContact.setOnClickListener(this);

        Button viewContact = rootView.findViewById(R.id.view_contact);
        viewContact.setOnClickListener(this);

        return rootView;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_contact:
                FragmentTransaction addTransaction = getParentFragmentManager().beginTransaction();
                addTransaction.replace(R.id.fragment_container, new AddContacts());
                addTransaction.commit();
                break;
            case R.id.view_contact:
                FragmentTransaction viewTransaction = getParentFragmentManager().beginTransaction();
                viewTransaction.replace(R.id.fragment_container, new ViewContactFragment());
                viewTransaction.commit();
                break;
        }
    }
}

