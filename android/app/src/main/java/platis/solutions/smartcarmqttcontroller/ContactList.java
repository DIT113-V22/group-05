package platis.solutions.smartcarmqttcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;

import platis.solutions.smartcarmqttcontroller.Data.DataBaseHelper;
import platis.solutions.smartcarmqttcontroller.Model.EmergencyContact;

public class ContactList extends AppCompatActivity {


    //Database
    ArrayAdapter customerArrayAdapter;
    DataBaseHelper dataBaseHelper;
    ListView lv_contactList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        Objects.requireNonNull(getSupportActionBar()).setTitle("EMERGENCY CONTACTS");  // provide compatibility to all the versions

        lv_contactList = findViewById(R.id.lv_contactList);
        dataBaseHelper = new DataBaseHelper(ContactList.this);
        ShowCustomerOnListView(dataBaseHelper);

        lv_contactList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int postion, long l) {
                EmergencyContact clickedCustomer = (EmergencyContact) parent.getItemAtPosition(postion);
                dataBaseHelper.deleteOne(clickedCustomer);
                ShowCustomerOnListView(dataBaseHelper);
                Toast.makeText(ContactList.this, "Deleted " + clickedCustomer.toString(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        }




    private void ShowCustomerOnListView(DataBaseHelper dataBaseHelper2) {
        customerArrayAdapter = new ArrayAdapter<EmergencyContact>(ContactList.this, android.R.layout.simple_expandable_list_item_1, dataBaseHelper2.getEveryone());
        lv_contactList.setAdapter(customerArrayAdapter);
    }



}