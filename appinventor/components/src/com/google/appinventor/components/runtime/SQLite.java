package com.google.appinventor.components.runtime;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage a private SQLite database.
 * blah blah blah
 *
 * @author M. Hossein Amerkashi
 */
@DesignerComponent(version = YaVersion.CLOCK_COMPONENT_VERSION,
    description = "This component can be used to manage your private database.",
    category = ComponentCategory.MISC, nonVisible = true, iconName = "images/sqlite.png")
@SimpleObject
public class SQLite extends AndroidNonvisibleComponent implements Component {
  String TAG = "SQLite";

  private SQLiteDatabase sqLiteDatabase;
  private String DB_NAME = "SQLite.sdb";
  private int DB_VERSION = 1;
  private Context context;
  private int rowsAffected = 0;

  public SQLite(ComponentContainer container) {
    super(container.$form());
    this.context = container.$context();

    MyDb myDb = new MyDb(context);
  }

  /**
   * Returns the number of rows affected during the last database activity; e.g. number of
   * rows deleted from the last delete statement
   *
   * @return the number of rows affected
   */
  @SimpleProperty(description = "Returns the number of rows affected",
      category = PropertyCategory.BEHAVIOR)
  public int RowsAffected() {
    return rowsAffected;
  }


  @SimpleFunction(description = "Used to run any valid SQLite query", userVisible = true)
  public YailList RunQuery(String query) {
    MyDb myDb = new MyDb(context);
    query = query.trim();
    YailList result;
    if (query.toLowerCase().startsWith("select") || query.toLowerCase().startsWith("explain")) {
      //This is readonly. We'll be using rawQuery which returns Cursor
      result = myDb.selectQuery(query);
    } else {
      //this use case is for handling statements that need writable database; e.g. update
      result = myDb.execSql(query);
    }
    return result;
  }

  @SimpleFunction(description = "Used to drop / delete table from database. Please note that " +
      "this event will DELETE any data you may have on the table and will then delete table from " +
      "the database. After this operation is completed, it can not be undone!", userVisible = true)
  public YailList DropTable(String tableName) {
    MyDb myDb = new MyDb(context);
    String query = "drop table if exists " + tableName;
    return RunQuery(query);
  }


  @SimpleFunction(description = "Used to retrieve list of existing tables from the database",
      userVisible = true)
  public YailList DisplayTables() {
    MyDb myDb = new MyDb(context);
//    String selectStatement="SELECT name table_name FROM sqlite_master WHERE type='table'";
    //We exclude system tables
    String selectStatement = "SELECT tbl_name FROM sqlite_master WHERE type='table' and tbl_name != 'android_metadata'";
    YailList result = myDb.selectQuery(selectStatement);

    return result;

  }

  /**
   * Method to determine the number of rows affected in the last database activity. We pass in the
   * SQLiteDatabase, because we want to know number of rows in the currently opened database
   * session; e.g. number of rows deleted
   *
   * @param db
   */
  private void determineRowsAffected(SQLiteDatabase db) {
    String sql = "select total_changes()";

    rowsAffected = 0;

    Cursor cursor = db.rawQuery(sql, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        rowsAffected = Integer.parseInt(cursor.getString(0));
      }
      cursor.close();
    }
  }

  public static String toCsvRow(String[] row) {
    String fieldDelim = ",";
    StringBuilder csvRow = new StringBuilder();
    for (String anItem : row) {
      csvRow.append(anItem).append(fieldDelim);
    }
    String result = csvRow.toString();
    if (result.endsWith(fieldDelim)) {
      result = result.substring(0, result.length() - 1);
    }
    return result;

  }

  private class MyDb extends SQLiteOpenHelper {

    public MyDb(Context context) {
      //constructor to check and see if our db exists or not.
      // If it does not, then it will call the onCreate method to create.
      // If it does, it will check the version. Here
      super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
      //creates our database, if it did not exist before
      //no-op
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
      //no op
    }


    private YailList selectQuery(String query) {
//      List<String> records = new ArrayList<String>();
//      YailList records = new YailList();
      List<String> records = new ArrayList<String>();

      String delimiter = ",";
      SQLiteDatabase db = this.getReadableDatabase();

      try {
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
          if (cursor.moveToFirst()) {
            //Get the column headings
            String[] columnNames = cursor.getColumnNames();
            records.add(toCsvRow(columnNames));
            StringBuilder sbRow;
            do {
              sbRow = new StringBuilder("");
              for (int i = 0; i < cursor.getColumnCount(); i++) {
                sbRow.append(cursor.getString(i)).append(delimiter);
              }
              String row = sbRow.toString();
              row = row.endsWith(delimiter) ? row.substring(0, row.length() - 1) : row;
              records.add(row);
            } while (cursor.moveToNext());

          }
          cursor.close();
        }

        //Now determine number of rows affected. This will update our property RowsAffected()
        determineRowsAffected(db);

      } catch (Exception e) {
        Log.e("-----------", "Error was: " + e.getMessage());
      }
      db.close();
//      db.releaseMemory();
      SQLiteDatabase.releaseMemory();

      //Convert our List<String> into YailList and report it back
      return YailList.makeList(records);
    }

   /*
    * execSQL usage: Execute a single SQL statement that is NOT a SELECT or any other
    * SQL statement that returns data. this will be used for INSERT, UPDATE, DELETE
    * rawQuery usage: Runs the provided SQL and returns a Cursor over the result set
    * (this will be used for UPDATE)
    */
    private YailList execSql(String sql) {
      List<String> result = new ArrayList<String>();

      try {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);

        //Now determine number of rows affected. This will update our property RowsAffected()
        determineRowsAffected(db);
        db.close();
        result.add("true");
      } catch (SQLException e) {
        Log.e(TAG, "Error executing query. Error is: " + e.getMessage());
        result.add("false");
      }

      //Convert our List<String> into YailList and report it back. Our list basically includes
      //only one entry. This has been done in this format to keep it consistent with other
      //methods that return resultset; e.g. result of select statement
      return YailList.makeList(result);
    }
  }

}
