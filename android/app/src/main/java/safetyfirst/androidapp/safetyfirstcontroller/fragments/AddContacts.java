package safetyfirst.androidapp.safetyfirstcontroller.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import safetyfirst.androidapp.safetyfirstcontroller.Data.DataBaseHelper;
import safetyfirst.androidapp.safetyfirstcontroller.Model.EmergencyContact;
import safetyfirst.androidapp.safetyfirstcontroller.R;

public class AddContacts extends Fragment implements View.OnClickListener {
    private EditText newContactPopupFirstname, newContactPopupLastname, newContactPopupMobile, newContactPopupEmail;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.popup, container, false);

        Button cancel = rootView.findViewById(R.id.backButton);
        cancel.setOnClickListener(this);

        Button save = rootView.findViewById(R.id.saveButton);
        save.setOnClickListener(this);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmergencyContact contactModel;
                Activity contactPopupView = new Activity();
                newContactPopupFirstname = rootView.findViewById(R.id.newcontactpopup_firstname);
                newContactPopupLastname = rootView.findViewById(R.id.newcontactpopup_lastname);
                newContactPopupMobile = rootView.findViewById(R.id.newcontactpopup_mobile);
                newContactPopupEmail = rootView.findViewById(R.id.newcontactpopup_email);
                //Throws exception if added contact information does not meet requirements.
                try {
                    contactModel = new EmergencyContact(-1, newContactPopupFirstname.getText().toString(),
                            newContactPopupLastname.getText().toString(),
                            Integer.parseInt(newContactPopupMobile.getText().toString()),
                            newContactPopupEmail.getText().toString());
                    Toast.makeText(getActivity(), contactModel.toString(), Toast.LENGTH_SHORT).show();
                    DataBaseHelper dataBaseHelper = new DataBaseHelper(getActivity());
                    boolean success = dataBaseHelper.addOne(contactModel);
                    Toast.makeText(getActivity(), "Contact Added", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error creating customer", Toast.LENGTH_SHORT).show();
                    contactModel = new EmergencyContact(-1, "error", "error", 0, "error");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new ContactsFragment());
                fragmentTransaction.commit();
            }
        });
        return rootView;
    }

    @Override
    public void onClick(View v) {

    }
}
