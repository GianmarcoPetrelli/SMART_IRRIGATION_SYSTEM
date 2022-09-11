package iet.progetto.iot;
//import iot.unipi.it.coap.resources.Actuator; IMPORT FOR COAP RESPONSE
//import iot.unipi.it.coap.resources.Thermometer; IMPORT FOR COAP RESPONSE

import java.sql.*;
import iet.progetto.iot.resources.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class DBManager {
    private static String DB_URL;
    private static String user;//root
    private static String password;//root
    private PreparedStatement pstRegNewSens;//register new sensor in db
    private PreparedStatement pstRegNewAct;//register new actuator in db
    private PreparedStatement pstRegTempObs;// register new temperature sample from sensor ID
    private PreparedStatement pstRegActActivation;// register new Activation record from actuator ID
    private PreparedStatement pstRegMWCObs;// register new MWC (ground humidity) sample from actuator ID
    private PreparedStatement pstUpdSensIPaddr;//update IPaddress from sensor ID
    private PreparedStatement pstUpdActIPaddr;//update IPaddress from actuator ID
    private PreparedStatement pstUpdActState;//update actuator state from actuator ID
    private PreparedStatement pstGetBindAct;//get binded actuator ID and related IPaddress from sensor ID
    private PreparedStatement pstGetLastActActivation; //get last activation record from actuator ID
    private PreparedStatement pstGetActState;//get actuator state from actuator ID
    private PreparedStatement pstGetLastnTempObs;//get last N temp. records from sensor ID
    private PreparedStatement pstGetLastnMWCObs;//get last N MWC. records from sensor ID


	public DBManager(String URL, String user, String password) {
        DB_URL = URL;
        DBManager.user = user;
        DBManager.password = password;

        
        try{
            Connection conn = DriverManager.getConnection(DB_URL, DBManager.user, DBManager.password);
            
            pstRegNewSens = conn.prepareStatement(
                "INSERT INTO `SensorDevice` (`SensorId`, `PairedAct`, `ZoneId`, `IPaddress`, `GeoCoordinates`)"+
                		" VALUES (?, ?, ?, ?, ?);"
            );
            pstRegNewAct = conn.prepareStatement(
                "INSERT INTO `ActuatorDevice` (`ActuatorId`, `ZoneId`, `IPaddress`, `State`, `GeoCoordinates`) "+
                		"VALUES (?, ?, ?, ?, ?);"
            );
            pstRegTempObs = conn.prepareStatement(
            		"INSERT INTO `Temperature` (`SensorId`, `Timestamp`, `Value_C`)"+
            				" VALUES (?, ?, ?);"

            );
            pstRegActActivation = conn.prepareStatement(
            		"INSERT INTO `ActuatorActivation` (`ActuatorId`, `Timestamp`)"+
            				" VALUES (?,?);"
                    );
            pstRegMWCObs = conn.prepareStatement(
                    "INSERT INTO `VolumetricWaterContent` (`SensorId`, `Timestamp`, `Value_perc`)"+
                    		" VALUES (?, ?, ?);"
                    );
            
            pstUpdSensIPaddr = conn.prepareStatement(
                    "UPDATE `SensorDevice`"+
                    		" SET `IPaddress`= ?"+
                    			" WHERE `SensorId`=?;"
                    );
            
            pstUpdActIPaddr = conn.prepareStatement(
                    "UPDATE `ActuatorDevice`"+
                    		" SET `IPaddress`=?"+
                    		" WHERE `ActuatorId`=?;"
                    );
            pstUpdActState = conn.prepareStatement(
                    "UPDATE `ActuatorDevice`"+
                    		" SET `State`=?"+
                    		" WHERE `ActuatorId`=?;"
                    );
            
            pstGetBindAct = conn.prepareStatement(
            		"SELECT AD.ActuatorId, AD.IPaddress"+
            				" FROM ActuatorDevice as AD inner join SensorDevice as SD on AD.ActuatorId = SD.PairedAct"+
            					" where SensorId = ?;"
            );
            pstGetLastActActivation = conn.prepareStatement(
            		"SELECT  MAX(Timestamp)"+
            				" FROM  `ActuatorActivation`"+
            					" where ActuatorId = ?;"
            );
            pstGetActState = conn.prepareStatement(
            		"SELECT State"+
            		" FROM `ActuatorDevice`"+
            		" where ActuatorId=?;"
            );
            pstGetLastnTempObs = conn.prepareStatement(
            		"SELECT Timestamp, Value_C"+
            				" FROM `Temperature`"+
            					" WHERE SensorId = ?"+
            						" order by Timestamp DESC"+
            							" LIMIT ?;"
                );
            pstGetLastnMWCObs = conn.prepareStatement(
            		"SELECT Timestamp, Value_perc"+
            				" FROM `VolumetricWaterContent`"+
            					" WHERE SensorId = ?"+
            						" order by Timestamp DESC"+
            							" LIMIT ?;"
                );
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

        

	public void RegNewActuator(String ActId, String ZoneId, String IPaddress,int State,String GeoCoordinates){
	    try{
	    	pstRegNewAct.setString(1, ActId);
	    	pstRegNewAct.setString(2, ZoneId);
	    	pstRegNewAct.setString(3, IPaddress);
	    	pstRegNewAct.setInt(4, State);
	    	pstRegNewAct.setString(5, GeoCoordinates);
	    	pstRegNewAct.executeUpdate();
	    }catch(SQLException e){
	        e.printStackTrace();
	    }
    }   
	
	public void RegNewSensor(String SensorId,String PairedAct, String ZoneId, String IPaddress,String GeoCoordinates){
	    try{
	    	pstRegNewSens.setString(1, SensorId);
	    	pstRegNewSens.setString(2, PairedAct);
	    	pstRegNewSens.setString(3, ZoneId);
	    	pstRegNewSens.setString(4, IPaddress);
	    	pstRegNewSens.setString(5, GeoCoordinates);
	    	pstRegNewSens.executeUpdate();
	    }catch(SQLException e){
	        e.printStackTrace();
	    }
    }   

	public void RegNewTempObs(String SensorId,String Timestamp, double temperature){
	    try{
	    	//valutare timestamp di sistema generato lato server java
	    	pstRegTempObs.setString(1, SensorId);
	    	pstRegTempObs.setString(2, Timestamp);
	    	pstRegTempObs.setDouble(3, temperature);
	    	pstRegTempObs.executeUpdate();
	    }catch(SQLException e){
	        e.printStackTrace();
	    }
	}
	

    public void RegActuatorActivation(String ActuatorId ,String Timestamp) {
    	  try{
  	    	//valutare timestamp di sistema generato lato server java
    		  pstRegActActivation.setString(1, ActuatorId);
    		  pstRegActActivation.setString(2, Timestamp);
    		  pstRegActActivation.executeUpdate();
  	    }catch(SQLException e){
  	        e.printStackTrace();
  	    }
  	}
    
public void RegNewMWCObs(String SensorId,String Timestamp, double humidity){
    try{
    	//valutare timestamp di sistema generato lato server java
    	pstRegMWCObs.setString(1, SensorId);
    	pstRegMWCObs.setString(2, Timestamp);
    	pstRegMWCObs.setDouble(3, humidity);
    	pstRegMWCObs.executeUpdate();
    }catch(SQLException e){
        e.printStackTrace();
    }
}

public void UpdSensIPaddr(String SensorId,String IPaddress){
    try{
    	//valutare timestamp di sistema generato lato server java
    	pstUpdSensIPaddr.setString(2, SensorId);
    	pstUpdSensIPaddr.setString(1, IPaddress);
    	pstUpdSensIPaddr.executeUpdate();
    }catch(SQLException e){
        e.printStackTrace();
    }
}

public void UpdActIPaddr(String ActuatorId,String IPaddress){
    try{
    	//valutare timestamp di sistema generato lato server java
    	pstUpdActIPaddr.setString(2, ActuatorId);
    	pstUpdActIPaddr.setString(1, IPaddress);
    	pstUpdActIPaddr.executeUpdate();
    }catch(SQLException e){
        e.printStackTrace();
    }
}

public void UpdActState(String ActuatorId,int State){
    try{
    	//valutare timestamp di sistema generato lato server java
    	pstUpdActState.setString(2, ActuatorId);
    	pstUpdActState.setInt(1, State);
    	pstUpdActState.executeUpdate();
    }catch(SQLException e){
        e.printStackTrace();
    }
}

public Actuator GetBindAct(String SensorId){
	Actuator res = null;
	try{
    	//valutare timestamp di sistema generato lato server java
    	pstGetBindAct.setString(1, SensorId);
    	ResultSet rs = pstGetBindAct.executeQuery();
    	while(rs.next())
    	{
    		res = new Actuator(rs.getString("IPaddress"), rs.getString("ActuatorId"));
    		break;
    	}
    }catch(SQLException e){
        e.printStackTrace();
    }
    return res;
}

public void GetLastActActivation(String ActuatorId){
    try{
    	//valutare timestamp di sistema generato lato server java
    	pstGetLastActActivation.setString(1, ActuatorId);
    	ResultSet rs = pstGetLastActActivation.executeQuery();
    	
        while(rs.next()){
        	System.out.println(rs.getString("MAX(Timestamp)"));
        }
        //ritorna oggetto datetime
        
    }catch(SQLException e){
        e.printStackTrace();
    }
}

public int GetActState(String ActuatorId){
    int state = -1;
	try{
    	//valutare timestamp di sistema generato lato server java
    	pstGetActState.setString(1, ActuatorId);
    	ResultSet rs = pstGetActState.executeQuery();
    	while(rs.next())
    	{
    		state = rs.getInt("State");
    		break;
    	}
        
    }catch(SQLException e){
        e.printStackTrace();
    }
	return state;
}

public double GetLastnTempObs(String SensorId, int n){
    double value = 0;
	try{
    	//valutare timestamp di sistema generato lato server java
    	pstGetLastnTempObs.setString(1, SensorId);
    	pstGetLastnTempObs.setInt(2, n);
    	ResultSet rs = pstGetLastnTempObs.executeQuery();
        
    	while(rs.next()){
        	//System.out.println(rs.getString("Timestamp"));
        	//System.out.println(rs.getString("Value_C"));
    		if(n==1) {
        		value = rs.getDouble("Value_C");
        		break;
        	}    		
        }
    	
    		
    }catch(SQLException e){
        e.printStackTrace();
    }
	return value;
}

public double GetLastnMWCObs(String SensorId, int n){
    double value=0;
	try{
    	//valutare timestamp di sistema generato lato server java
    	pstGetLastnMWCObs.setString(1, SensorId);
    	pstGetLastnMWCObs.setInt(2, n);
    	ResultSet rs = pstGetLastnMWCObs.executeQuery();
    	while(rs.next()){
        	//System.out.println(rs.getString("Timestamp"));
        	//System.out.println(rs.getString("Value_C"));
    		if(n==1) {
    			value = rs.getDouble("Value_perc");
        		break;
        	}    		
        }
    	
    }catch(SQLException e){
        e.printStackTrace();
    }
	return value;
}
     
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		DBManager db = new DBManager("jdbc:mysql://localhost/smart_irrigation_db_nuovo","JavaCollector","password");
		db.UpdActIPaddr("Act1","fd00::212:4b00:f82:8a06");
		//db.RegNewSensor("7", "act1", "zona1", "fe80::212:4b00:f82:8a06", "(12;32)");
		//db.RegNewSensor("sens1", "act1", "zona1", "fe80::212:4b00:f82:8a06", "(12;32)");
		//db.RegNewActuator("act1", "zona1", "123::1:1:1.1",-1, "(12;32)");
		//db.UpdActState("act1", 0);
		//db.RegNewActuator(ActId, ZoneId, IPaddress, State, GeoCoordinates);
		/*
		db.RegNewActuator("pJavaConnect2","provaJavaSDK","123::1:1:1.1",1,"(12;32)");
		db.RegNewSensor("pJava1", "pJavaConnect2", "provaJavaSDK", "123::1:1:1.1", "(12;32)");
		
		System.out.println("-------test bind--------");
		db.GetBindAct("pJava1");
		System.out.println("-------test act activation--------");
		db.RegActuatorActivation("pJavaConnect2", "2020-08-09 12:56:00");
		db.RegActuatorActivation("pJavaConnect2", "2020-08-08 12:46:00");
		db.RegActuatorActivation("pJavaConnect2", "2020-08-19 12:56:00");
		System.out.println("-------test get last act--------");
		db.GetLastActActivation("pJavaConnect2");
		System.out.println("-------test get act state--------");
		db.GetActState("pJavaConnect2");
		System.out.println("-------test state upd--------");
		db.UpdActState("pJavaConnect2",2);
		System.out.println("------test get act state---------");
		db.GetActState("pJavaConnect2");
		System.out.println("---------------");
		*/
			
		/*
		while(true) {
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String timest = Timestamp.from(Instant.now()).toString();
			//db.RegActuatorActivation("pJavaConnect2", timest);
			//db.RegActuatorActivation("act1", timest);
			db.RegNewTempObs("sens1", timest, 12);
			db.RegNewMWCObs("sens1", timest, 99);
		}*/
		
		
		//db.RegNewTempObs("pJava1","2020-08-09 13:56:00",12);
		//db.RegNewTempObs("pJava1","2020-08-09 14:56:00",12);
		//db.GetLastnTempObs("pJava1", 2);
		//System.out.println("---------------");

		//db.GetLastActActivation("pJavaConnect2");

		


		
		
	}

}
