package com.nic.webdesk;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*----------------------------------------------------------------------
WebdeskHelper - methods for connection, create and update tables of sqlite db webdesk
tables:
- user      login, username and password crypted
- log       login message
- webdesk   date of app
- message   internal message
--------------------------------------------------------------------- */

public class WebdeskDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "webdesk.db";
    private static final int DB_VERSION = 2;
    public WebdeskDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //------------------------------------------------------------ create
    @Override
    public void onCreate(SQLiteDatabase db) {
        createUser(db);
        createLog(db);
        createWebDesk(db);
        createMessage(db);
    }

    //------------------------------------------------------------ Upgrade
    /*
    For recreate the table change the version number
    Change version of the db, all the tablkes are deleted and recreate
    DB_VERSION = n -> n+1;
    */
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS log");
        db.execSQL("DROP TABLE IF EXISTS webdesk");
        db.execSQL("DROP TABLE IF EXISTS message");
        onCreate(db); // Recreate all tables
    }

    //============================================================ 1 - Table User
    // no delete table if exists
    public void createUser(SQLiteDatabase db) {
        try {
            //------------------------------------------------- delete table if exist
            //db.execSQL("DROP TABLE IF EXISTS user" );
            //------------------------------------------------- verify if table exist
            boolean tableExists = checkIfTableExists(db, "user");
            if (tableExists) {
                System.out.println("Database - La tabella user esiste già nel Database.");
            } else {
                String query = "CREATE TABLE user (" +
                        "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "saltUser BLOB, " +
                        "hashUser TEXT, " +
                        "saltPw BLOB, " +
                        "hashPw TEXT)";
                db.execSQL(query);
            }
        } catch (Exception e) {
            Log.e("DB_CREATE", "Errore nella creazione della tabella user ", e);
        }
    }

    //============================================================ 2 - Table Log
    public void createLog(SQLiteDatabase db) {
        try {
            //------------------------------------------------- delete table if exist
            db.execSQL("DROP TABLE IF EXISTS log");
            //------------------------------------------------- verify if table exist
            boolean tableExists = checkIfTableExists(db, "log");
            if (tableExists) {
                System.out.println("Database - La tabella log esiste già nel Database.");
            } else {
                String query = "CREATE TABLE log (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "date TEXT, " +
                        "type INTEGER, " +
                        "status TEXT, " +
                        "note TEXT)";
                db.execSQL(query);
            }
        } catch (Exception e) {
            Log.e("DB_CREATE", "Errore nella creazione della tabella log ", e);
        }
    }

    //============================================================ 3 - Table WebDesk
    public void createWebDesk(SQLiteDatabase db) {
        try {
            //------------------------------------------------- delete table if exist
            db.execSQL("DROP TABLE IF EXISTS webdesk ");
            //------------------------------------------------- verify if table exist
            boolean tableExists = checkIfTableExists(db, "webdesk");
            if (tableExists) {
                System.out.println("Database - La tabella webdesk esiste già nel Database.");
            } else {
                String query = "CREATE TABLE webdesk (" +
                        "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "UserCod INTEGER, " +
                        "Name TEXT, " +
                        "Url TEXT, " +
                        "Icon TEXT, " +
                        "Type1 TEXT, " +
                        "Type2 TEXT, " +
                        "Note TEXT, " +
                        "Order1 INTEGER, " +
                        "Order2 INTEGER, " +
                        "DateCreate TEXT, " +
                        "DateVisit TEXT, " +
                        "Frequency INTEGER, " +
                        "TextColor TEXT, " +
                        "Background TEXT, " +
                        "Flag1 INTEGER, " +
                        "Flag2 INTEGER)";
                db.execSQL(query);
            }
        } catch (Exception e) {
            Log.e("DB_CREATE", "Errore nella creazione della tabella webdesk ", e);
        }
    }

    //============================================================== 4 - Table Message
    // attualmente usato per trasferire il messaggio di importazione dei dati .csv da SQLiteDb alla ActivityTool
    public void createMessage(SQLiteDatabase db) {
        try {
            //------------------------------------------------- delete table if exist
            db.execSQL("DROP TABLE IF EXISTS message ");
            //------------------------------------------------- verify if table exist
            boolean tableExists = checkIfTableExists(db, "message");
            if (tableExists) {
                System.out.println("Database - La tabella message esiste già nel Database.");
            } else {
                String createTableMessage = "CREATE TABLE IF NOT EXISTS message ("
                        + "id INTEGER PRIMARY KEY, "
                        + "message TEXT)";
                db.execSQL(createTableMessage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //============================================================= Service methods
    
        //--------------------------------------------------------- checkIfTableExists
        // Metod to verify if table exist
        private boolean checkIfTableExists(SQLiteDatabase db, String tableName) {
            boolean exists = false;
            Cursor cursor = null;
            try {
                String query = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=" +
                        DatabaseUtils.sqlEscapeString(tableName);
                cursor = db.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    exists = cursor.getInt(0) > 0;
                }
            } catch (Exception e) {
                Log.e("DB_CHECK_TABLE", "Errore nel controllo della tabella: " + tableName, e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return exists;
        }

    }

