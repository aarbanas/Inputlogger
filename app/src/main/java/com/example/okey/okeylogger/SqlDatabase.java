package com.example.okey.okeylogger;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SqlDatabase extends SQLiteOpenHelper {

    private Context ctx;
    private String mfilepath;
    private String resultID;

    //database, table and column names

    public static final String TABLE_NAME = "phrases";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USED = "used";
    public static final String COLUMN_PHRASE = "phrase";

    public static final String DATABASE_NAME = "phrases.db";
    public static final int DATABASE_VERSION = 1;

    //creating the database

    public static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USED + " INTEGER NOT NULL DEFAULT 0, "
            + COLUMN_PHRASE + " TEXT NOT NULL);";

    public SqlDatabase(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("Database", "proslo");
        ctx = context;
    }

    public SqlDatabase(Context context, String filepath){

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
        mfilepath = filepath;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("Database", "proslo2");
        db.execSQL(DATABASE_CREATE);
        populateDB(db);
        Log.d("Database", "Database ready!");
        Toast.makeText(ctx, "Database ready!", Toast.LENGTH_SHORT).show();
    }

    public String getRow(){

        String query;

        SQLiteDatabase db = getReadableDatabase();
        query = "SELECT " + COLUMN_PHRASE + "," + COLUMN_ID + " FROM " + TABLE_NAME + " WHERE " + COLUMN_USED + " = 0 " +
                " ORDER BY random() LIMIT 1;";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        String result = cursor.getString(cursor.getColumnIndex(COLUMN_PHRASE));
        resultID = cursor.getString(cursor.getColumnIndex(COLUMN_ID));

        cursor.close();
        db.close();

        return result;
    }

    public String getPhraseID(){

        return resultID;
    }

    private void populateDB(SQLiteDatabase db) {

        if(mfilepath == null) {

            AssetManager asset = ctx.getAssets();
            InputStream istream = null;
            String fileName = "phrases.txt";
            String line;
            BufferedReader br;

            try {

                istream = asset.open(fileName);

            } catch (IOException e) {

                e.printStackTrace();
            }

            br = new BufferedReader(new InputStreamReader(istream));


            db.beginTransaction();
            int i = 0;
            try {

                while ((line = br.readLine()) != null) {

                    ContentValues cv = new ContentValues(DATABASE_VERSION);
                    cv.put(COLUMN_PHRASE, line.trim());
                    db.insert(TABLE_NAME, null, cv);
                    i++;
                }

            } catch (IOException e) {

                e.printStackTrace();
            }
        }else{

            //deleteDatabase();
            String line2;
            BufferedReader br2;
            File source = new File(mfilepath);

            try{

                br2 = new BufferedReader(new FileReader(source));
                db.beginTransaction();
                int j= 0;
                while ((line2 = br2.readLine()) != null) {

                    ContentValues cv = new ContentValues(DATABASE_VERSION);
                    cv.put(COLUMN_PHRASE, line2.trim());
                    db.insert(TABLE_NAME, null, cv);
                    j++;
                }
                Log.d("Database", String.valueOf(j));
            }catch(IOException e){

                e.printStackTrace();
            }

        }

        db.setTransactionSuccessful();
        db.endTransaction();
        Log.d("Database", "pupulating succesfull! !");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void deleteDatabase() {

        ctx.deleteDatabase(DATABASE_NAME);
        Log.d("Database", "Database deleted!");
    }

    public void upgradePhraseUsage(String phraseID){

        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_USED + "= 1 WHERE " + COLUMN_ID + "=" + phraseID);
    }
}
