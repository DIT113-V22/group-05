package safetyfirst.androidapp.safetyfirstcontroller.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import safetyfirst.androidapp.safetyfirstcontroller.Model.EmergencyContact;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String CONTACT_TABLE = "CONTACT_TABLE";
    public static final String COLUMN_CONTACT_FIRSTNAME = "CONTACT_NAME";
    public static final String COLUMN_CONTACT_LASTNAME = "CONTACT_LASTNAME";
    public static final String COLUMN_CONTACT_PHONE_NUMBER = "CONTACT_PHONE_NUMBER";
    public static final String COLUMN_CONTACT_EMAIL = "CONTACT_EMAIL";
    public static final String COLUMN_ID = "ID";

    public DataBaseHelper(@Nullable Context context) {
        super(context, "contact_test.db", null, 1);
    }

    //this is called the first time a database is accessed. There should be code in here to create a new database.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + CONTACT_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_CONTACT_FIRSTNAME + " TEXT, " + COLUMN_CONTACT_LASTNAME + " TEXT, " + COLUMN_CONTACT_PHONE_NUMBER + " LONG, " + COLUMN_CONTACT_EMAIL + " TEXT)";
        db.execSQL(createTableStatement);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //db.execSQL("DROP TABLE IF EXISTS " + CONTACT_TABLE);
        //To be implemented
    }


    public boolean addOne(EmergencyContact contactModel){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CONTACT_FIRSTNAME, contactModel.getFirst_name());
        cv.put(COLUMN_CONTACT_LASTNAME, contactModel.getLastName());
        cv.put(COLUMN_CONTACT_PHONE_NUMBER, contactModel.getPhoneNumber());
        cv.put(COLUMN_CONTACT_EMAIL, contactModel.getEmail());


        long insert = db.insert(CONTACT_TABLE, null, cv);
        if(insert == -1){
            return false;
        }else{
            return true;
        }

    }


    public List<EmergencyContact> getEveryone(){

        List<EmergencyContact> returnList = new ArrayList<>();

        //get data from the database

        String queryString = "SELECT * FROM " + CONTACT_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()){
            //loop through the cursor (result set) and create new customer objects. Put them into the return list.
            do{
                int contactID = cursor.getInt(0);
                String contact_firstName = cursor.getString(1);
                String contact_lastName = cursor.getString(2);
                int contact_phoneNumber = cursor.getInt(3);
                String contact_email = cursor.getString(4);

                //boolean customerActive = cursor.getInt(3) == 1 ? true: false;

                EmergencyContact contact = new EmergencyContact(contactID, contact_firstName, contact_lastName, contact_phoneNumber, contact_email);
                returnList.add(contact);

            } while (cursor.moveToNext());


        }else{
            //failure. do not add anything to the list.
        }

        //close both the cursor and db when done

        cursor.close();
        db.close();

        return returnList;

    }

    public boolean deleteOne(EmergencyContact contactModel){
        //find EmergencyContact in the database. If it is found, delete it and return true.
        //if it is not found, return false

        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + CONTACT_TABLE + " WHERE " + COLUMN_ID + " = " + contactModel.getId();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToNext()){
            return true;

        }else{
            return false;
        }


    }






}
