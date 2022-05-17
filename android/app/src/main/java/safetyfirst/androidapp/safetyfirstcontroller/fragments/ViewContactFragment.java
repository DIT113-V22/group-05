package safetyfirst.androidapp.safetyfirstcontroller.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import safetyfirst.androidapp.safetyfirstcontroller.Data.DataBaseHelper;
import safetyfirst.androidapp.safetyfirstcontroller.Model.EmergencyContact;
import safetyfirst.androidapp.safetyfirstcontroller.R;

public class ViewContactFragment extends Fragment {
    //Database
    ArrayAdapter customerArrayAdapter;
    DataBaseHelper dataBaseHelper;
    ListView lv_contactList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_emergency_contacts, container, false);

        lv_contactList = rootView.findViewById(R.id.lv_contactList);
        dataBaseHelper = new DataBaseHelper(getContext());
        ShowCustomerOnListView(dataBaseHelper);

        lv_contactList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int postion, long l) {
                EmergencyContact clickedCustomer = (EmergencyContact) parent.getItemAtPosition(postion);
                dataBaseHelper.deleteOne(clickedCustomer);
                ShowCustomerOnListView(dataBaseHelper);
                Toast.makeText(getContext(), "Deleted " + clickedCustomer.toString(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        return rootView;
    }

    private void ShowCustomerOnListView(DataBaseHelper dataBaseHelper2) {
        customerArrayAdapter = new ArrayAdapter<EmergencyContact>(this.getContext(),
                android.R.layout.simple_expandable_list_item_1,
                dataBaseHelper2.getEveryone());
        lv_contactList.setAdapter(customerArrayAdapter);
    }
}