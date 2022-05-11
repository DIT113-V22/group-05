package safetyfirst.androidapp.safetyfirstcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;

import safetyfirst.androidapp.safetyfirstcontroller.Data.DataBaseHelper;
import safetyfirst.androidapp.safetyfirstcontroller.Model.EmergencyContact;

public class ContactList extends AppCompatActivity {


    //Database
    ArrayAdapter customerArrayAdapter;
    DataBaseHelper dataBaseHelper;
    ListView lv_contactList;

    //Buttons
    Button delete_contact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        Objects.requireNonNull(getSupportActionBar()).setTitle("EMERGENCY CONTACTS");  // provide compatibility to all the versions

        lv_contactList = findViewById(R.id.lv_contactList);
        dataBaseHelper = new DataBaseHelper(ContactList.this);
        ShowCustomerOnListView(dataBaseHelper);

        Button deleteButton = (Button) findViewById(R.id.button2);


        lv_contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {

                deleteButton.setOnClickListener(
                        new Button.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EmergencyContact clickedCustomer = (EmergencyContact) parent.getItemAtPosition(postion);
                                dataBaseHelper.deleteOne(clickedCustomer);
                                ShowCustomerOnListView(dataBaseHelper);
                                Toast.makeText(ContactList.this, "Deleted " + clickedCustomer.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );

            }
        });

        }

    private void ShowCustomerOnListView(DataBaseHelper dataBaseHelper2) {
        customerArrayAdapter = new ArrayAdapter<EmergencyContact>(ContactList.this, android.R.layout.simple_expandable_list_item_1, dataBaseHelper2.getEveryone());
        lv_contactList.setAdapter(customerArrayAdapter);
    }



}