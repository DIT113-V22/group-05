package safetyfirst.androidapp.safetyfirstcontroller.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import safetyfirst.androidapp.safetyfirstcontroller.Data.DataBaseHelper;
import safetyfirst.androidapp.safetyfirstcontroller.Model.EmergencyContact;
import safetyfirst.androidapp.safetyfirstcontroller.R;

public class ViewContactFragment extends Fragment {
    //Database
    ArrayAdapter customerArrayAdapter;
    DataBaseHelper dataBaseHelper;
    ListView contactList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_emergency_contacts, container, false);

        contactList = rootView.findViewById(R.id.lv_contactList);
        dataBaseHelper = new DataBaseHelper(getContext());
        ShowCustomerOnListView(dataBaseHelper);

        Button deleteButton = (Button) rootView.findViewById(R.id.button2);
        Button backButton = (Button) rootView.findViewById(R.id.cancel_view);

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {

                deleteButton.setOnClickListener(
                        new Button.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EmergencyContact clickedCustomer = (EmergencyContact) parent.getItemAtPosition(postion);
                                dataBaseHelper.deleteOne(clickedCustomer);
                                ShowCustomerOnListView(dataBaseHelper);
                                Toast.makeText(getContext(), "Deleted " + clickedCustomer.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new ContactsFragment());
                fragmentTransaction.commit();
            }
        });
        return rootView;
    }

    private void ShowCustomerOnListView(DataBaseHelper dataBaseHelper) {
        customerArrayAdapter = new ArrayAdapter<EmergencyContact>(this.getContext(),
                android.R.layout.simple_expandable_list_item_1,
                dataBaseHelper.getEveryone());
        contactList.setAdapter(customerArrayAdapter);
    }
}
