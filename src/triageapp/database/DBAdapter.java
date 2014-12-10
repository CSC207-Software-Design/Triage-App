
package triageapp.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;


/**
 * Triage database helper class. Has the ability to
 * create the DB, open a connection to the DB, and close the 
 * connection, and well as check if a connection is open or not.
 */
public class DBAdapter {
	
	/** Name of Database file. */
	public static final String DATABASE_NAME = "triageApp_DB";
	protected static final int DATABASE_VERSION = 2;
	/** Tag used for log messages related to the Database */
	protected static final String TAG = "TriageAppDbAdapter";
	
	/** SQLiteOpenHelper class. */
	protected DatabaseHelper mDbHelper;
	/** The SQLite databse. */
	protected SQLiteDatabase mDb;
	/** The context to use to create or open the database. */
	protected final Context context; 
	
	// Database creation sql statements
	
	/** Sql statement for the creation of the Role table. */
	protected static final String DATABASE_CREATE_ROLE =
			"CREATE TABLE Role (" +
					"_ID	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
					"roleName	TEXT NOT NULL UNIQUE" +
				");";
	/** Sql statement for the creation of the Patient table. */
	protected static final String DATABASE_CREATE_PATIENT =
			"CREATE TABLE Patient (" +
					"_ID	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
					"healthCardNumber	TEXT NOT NULL UNIQUE, " +
					"name	TEXT NOT NULL, " +
					"dob	TEXT NOT NULL" +
				");";
	/** Sql statement for the creation of the ERVisit table. */
	protected static final String DATABASE_CREATE_ERVISIT =
			"CREATE TABLE ERVisit (" +
					"_ID	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
					"PatientID	INTEGER NOT NULL, " +
					"arrivalTime	INTEGER NOT NULL, " +
					"timeSeenByDoctor	INTEGER, " +
					"isClosed	INTEGER NOT NULL DEFAULT 0, " + //FALSE
					"urgency	INTEGER NOT NULL DEFAULT 0, " + //!!!!!!!!!!!!
					"FOREIGN KEY (PatientID) REFERENCES Patient(_ID) ON DELETE CASCADE" + 
				");";
	/** Sql statement for the creation of the VitalSigns table. */
	protected static final String DATABASE_CREATE_VITALSIGNS =
			"CREATE TABLE VitalSigns (" +
					"_ID	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
					"ERVisitID	INTEGER NOT NULL, " +
					"systolic	INTEGER NOT NULL, " +
					"diastolic	INTEGER NOT NULL, " +
					"temperature	REAL NOT NULL, " +
					"heartRate	REAL NOT NULL, " +
					"timestamp	INTEGER NOT NULL, " +
					"FOREIGN KEY (ERVisitID) REFERENCES ERVisit(_ID) ON DELETE CASCADE" + 
				");";
	/** Sql statement for the creation of the User table. */
	protected static final String DATABASE_CREATE_USER =				
			"CREATE TABLE User (" +
					"_ID	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
					"username	TEXT NOT NULL UNIQUE, " +
					"password	TEXT NOT NULL, " +
					"RoleID	INTEGER NOT NULL, " +
					"FOREIGN KEY (RoleID) REFERENCES Role(_ID) ON DELETE CASCADE" + 
				");";
	/** Sql statement for the creation of the Prescription table. */
	protected static final String DATABASE_CREATE_PRESCRIPTION =	
			"CREATE TABLE Prescription (" +
					"_ID	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
					"ERVisitID	INTEGER NOT NULL, " +
					"medication	TEXT NOT NULL, " +
					"instructions	TEXT NOT NULL, " +
					"FOREIGN KEY (ERVisitID) REFERENCES ERVisit(_ID) ON DELETE CASCADE" + 
				");";
	
	/**
     * Constructs a DBAdapter object and takes the context 
     * to allow the database to be opened/created.
     * @param ctx The Context within which to work.
     */
    public DBAdapter(Context ctx)
    {
        this.context = ctx;  
    }
    
    /**
     * A helper class to manage database creation and version management. 
     * Handles creating all the DB tables using the creation statements
     * and populating the Role table with default values.
     */
    protected static class DatabaseHelper extends SQLiteOpenHelper 
    {
    	/** Insertion statement for the Role table */
    	private static final String INSERT_ROLE = "INSERT INTO Role (roleName) VALUES (?)";
    	
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	
        	db.execSQL("PRAGMA foreign_keys=ON;");
        	
        	//create DB tables
        	db.execSQL(DATABASE_CREATE_ROLE);   
            db.execSQL(DATABASE_CREATE_PATIENT);
            db.execSQL(DATABASE_CREATE_ERVISIT);
            db.execSQL(DATABASE_CREATE_VITALSIGNS); 
            db.execSQL(DATABASE_CREATE_USER); 
            db.execSQL(DATABASE_CREATE_PRESCRIPTION); 
            
            //Inserts nurse and physician role Strings into Role Table.
            SQLiteStatement statement = db.compileStatement(INSERT_ROLE);
            statement.bindString(1, "nurse");
            statement.executeInsert();
            statement.bindString(1, "physician");
            statement.executeInsert();
            statement.close();
            
        }
        
        @Override
        public void onOpen(SQLiteDatabase db){
        	super.onOpen(db);
        	db.execSQL("PRAGMA foreign_keys=ON;");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {           
        	Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        	db.execSQL("DROP TABLE IF EXISTS Patient");
            db.execSQL("DROP TABLE IF EXISTS Role");
            db.execSQL("DROP TABLE IF EXISTS ERVisit");
            db.execSQL("DROP TABLE IF EXISTS VitalSigns");
            db.execSQL("DROP TABLE IF EXISTS User");
            db.execSQL("DROP TABLE IF EXISTS UrgencyLevel");
            db.execSQL("DROP TABLE IF EXISTS Prescription");
            onCreate(db);
        }
    } 
    
    
   /** Creates the Database if it does not exist.
    * Open a connection to the Database.
    * @return this
    * @throws SQLException
    * return type: DBAdapter.
    */

    /**
     * Open the Triage+ app database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure.
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DBAdapter open() throws SQLException 
    {
    	if (mDbHelper == null) {
    		mDbHelper = new DatabaseHelper(context);
    	}
    	if (!isOpen())
    		mDb = mDbHelper.getWritableDatabase();

    	return this;
    }

   /**
    * Checks if the database is currently open.
    * @return true if a connection the database has been opened.
    */
   public boolean isOpen() {
       return mDb != null && mDb.isOpen();
   }

   /**
    * Close the connection the the database. 
    */
   public void close() 
   {
	   mDbHelper.close();
   }
	
}
