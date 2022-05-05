package platis.solutions.smartcarmqttcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import platis.solutions.smartcarmqttcontroller.Data.DataBaseHelper;
import platis.solutions.smartcarmqttcontroller.Model.EmergencyContact;

public class ContactList extends AppCompatActivity {

    private Button view_contacts;

    ArrayAdapter customerArrayAdapter;
    DataBaseHelper dataBaseHelper;
    ListView lv_contactList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        lv_contactList = findViewById(R.id.lv_contactList);
        view_contacts = findViewById(R.id.btn_viewAll);
        dataBaseHelper = new DataBaseHelper(ContactList.this);
        ShowCustomerOnListView(dataBaseHelper);

        view_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DataBaseHelper dataBaseHelper = new DataBaseHelper(ContactList.this);
                ShowCustomerOnListView(dataBaseHelper);

            }
        });

    }


    private void ShowCustomerOnListView(DataBaseHelper dataBaseHelper2) {
        customerArrayAdapter = new ArrayAdapter<EmergencyContact>(ContactList.this, android.R.layout.simple_expandable_list_item_1, dataBaseHelper2.getEveryone());
        lv_contactList.setAdapter(customerArrayAdapter);

    }



}