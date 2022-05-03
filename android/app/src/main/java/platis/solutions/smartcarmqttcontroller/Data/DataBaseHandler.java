package platis.solutions.smartcarmqttcontroller.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import platis.solutions.smartcarmqttcontroller.Model.Item;

public class DataBaseHandler extends SQLiteOpenHelper {

    private final ArrayList<Item> db_list = new ArrayList<>();


    public DataBaseHandler(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQLITETABLE = "CREATE TABLE " +
                Constants.TABLE_NAME + "( " +
                Constants.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.DATABASE_EDITTEXT + " STRING); ";

        sqLiteDatabase.execSQL(SQLITETABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME );

        onCreate(sqLiteDatabase);

    }

    public void Save(Item item){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.DATABASE_EDITTEXT, item.getInput());

        db.insert(Constants.TABLE_NAME, null, values);

    };

    public ArrayList<Item>getallItems () {

        db_list.clear();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_NAME, new String[] {Constants.KEY_ID, Constants.DATABASE_EDITTEXT}, null, null, null, null, null);

        if (cursor.moveToFirst()){
            do {
                Item item = new Item();
                item.setInput(cursor.getString(cursor.getColumnIndex(Constants.DATABASE_EDITTEXT)));

                db_list.add(item);
            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return db_list;

    }

    public void deleteItem(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_NAME, Constants.KEY_ID + "=?", new String[] {String.valueOf(id)});
    }

}
