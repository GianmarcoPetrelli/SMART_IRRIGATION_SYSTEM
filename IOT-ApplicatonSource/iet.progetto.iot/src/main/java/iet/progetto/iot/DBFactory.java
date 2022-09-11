package iet.progetto.iot;

import java.sql.Timestamp;
import java.util.Date;
	
public class DBFactory {
	
    private DBFactory(){};//empty constructor; this class manages db connections as singleton
    private static DBManager dbInstance = null;

    public static DBManager createDBManager(String URL, String user, String password) {
        if(dbInstance == null)
            DBFactory.dbInstance = new DBManager(URL, user, password);
        return DBFactory.dbInstance;
    }

    public static DBManager getDbInstance(){
        return DBFactory.dbInstance;
    }
        
	  

    public static void main(String args[])
    {
        
        // getting the system date
        Date date = new Date();
        
        // getting the object of the Timestamp class
        Timestamp ts = new Timestamp(date.getTime());
        
        // printing the timestamp of the current date
        System.out.println(ts);
    }

    
}
