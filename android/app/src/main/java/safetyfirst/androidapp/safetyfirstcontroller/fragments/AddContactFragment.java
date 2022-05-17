package safetyfirst.androidapp.safetyfirstcontroller.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import safetyfirst.androidapp.safetyfirstcontroller.Data.DataBaseHelper;
import safetyfirst.androidapp.safetyfirstcontroller.Model.EmergencyContact;
import safetyfirst.androidapp.safetyfirstcontroller.R;

public class AddContactFragment extends Fragment implements View.OnClickListener {
    private AlertDialog dialog;
    private EditText newcontactpopup_firstname, newcontactpopup_lastname, newcontactpopup_mobile, newcontactpopup_email;
    private Button newcontactpopup_cancel, newcontactpopup_save;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.popup, container, false);

        Button cancel = rootView.findViewById(R.id.cancelButton);
        cancel.setOnClickListener(this);

        Button save = rootView.findViewById(R.id.saveButton);
        save.setOnClickListener(this);

        return rootView;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancelButton:
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new ContactsFragment());
                fragmentTransaction.commit();
                break;
            case R.id.saveButton:
                EmergencyContact contactModel;
                //Throws exception if added contact information does not meet requirements.
                try {
                    contactModel = new EmergencyContact(-1, newcontactpopup_firstname.getText().toString(),
                            newcontactpopup_lastname.getText().toString(),
                            Integer.parseInt(newcontactpopup_mobile.getText().toString()),
                            newcontactpopup_email.getText().toString());
                    Toast.makeText(this.getActivity(), contactModel.toString(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(this.getActivity(), "Contact Added", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(this.getActivity(), "Error creating customer", Toast.LENGTH_SHORT).show();
                    contactModel = new EmergencyContact(-1, "error", "error", 0, "error");
                }
                DataBaseHelper dataBaseHelper = new DataBaseHelper(this.getContext());

                boolean success = dataBaseHelper.addOne(contactModel);
                break;
        }
    }
}