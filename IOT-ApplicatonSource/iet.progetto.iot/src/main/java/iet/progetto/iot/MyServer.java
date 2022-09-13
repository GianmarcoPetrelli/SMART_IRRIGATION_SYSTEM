package iet.progetto.iot;



import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import java.time.Month;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.paho.client.mqttv3.*;

import iet.progetto.iot.resources.*;

public class MyServer extends CoapServer {
	private static String url = "jdbc:mysql://localhost/smart_irrigation_db_nuovo";
	private static String username = "root";//"JavaCollector";
	private static String password = "password";
	
	
	private static DBManager db = null;
	private static MqttCollector mqtt = null;
	private static SimulationTime smt = null;
	public static void main(String[] args) {
		
		//DBManager db = new DBManager("jdbc:mysql://localhost/smart_irrigation_db_nuovo","JavaCollector","password");

		db =  DBFactory.createDBManager(url,username,password);
		smt = SimulationTimeFactory.createSimulationTimeManager(8,0,0,Duration.ofHours(12),Duration.ofMinutes(10));
		smt.startSimulationTimer();

		System.out.println("************* SMART AGRICOLTURE APPLICATION **********");
		
		MyServer server = new MyServer();
		//server.add(new CoAPResourceExample("Hello"));
		//server.start();
		
		//runCoapCollector();
	    //runMqttCollector();
		//server.destroy();
		db.UpdActState("Act1", 0);
		db.UpdActState("Act2", 0);
		

		
		System.out.println("Esecuzione parte mqtt...");
		mqtt = MqttFactory.createMqttManager();
		System.out.println("Esecuzione osservatori CoAP...");
		CoapObserver obs1 = new CoapObserver("Act1","fd00::212:4b00:f82:8a06");
		obs1.startObserving();
		CoapObserver obs2 = new CoapObserver("Act2","fd00::212:4b00:f82:bf06");
		obs2.startObserving();
		
		while(true);
	}


    
	public static Integer insertInputLine() {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		try {
			String line = bufferedReader.readLine();
			Integer value = -1;
			if (isNumeric(line))
				value = Integer.parseInt(line);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null)
			return false;
		try {
			@SuppressWarnings("unused")
			Integer number = Integer.parseInt(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
    
}



class CoapObserver {
	private CoapClient client;
	private DBManager db = DBFactory.getDbInstance();
	private boolean notWorking = false;
	private String actuatorId;
	private MqttCollector mqtt = MqttFactory.getDbInstance();
	private SimulationTime smt = SimulationTimeFactory.getDbInstance();
	
	public CoapObserver(String actuatorId,String ipv6Address) {
		client = new CoapClient("coap://["+ipv6Address+"]/sensors/actuator");
		this.actuatorId = actuatorId;
	}
	
	public void startObserving() {
		CoapObserveRelation relation = client.observe(
			    new CoapHandler(){
			    	//@Override
			        public void onLoad(CoapResponse response){
			            String content = response.getResponseText();
			            System.out.println("INFO: Lato COAP, ottenuto:" + content);
			            JSONObject responseText = new JSONObject(content);
			            
			            int actuatorState = responseText.getInt("state");
			            System.out.println("INFO: lo stato è quindi:" + actuatorState);
			            if(actuatorState == 1) {
				            //String timest = Timestamp.from(Instant.now()).toString();
				            String timest = smt.getCurrentSimulatedDateTime();
				            db.UpdActState(actuatorId, 1);
				            db.RegActuatorActivation(actuatorId, timest);
				            System.out.println("Faccio un publish di avvio simulazione");
				            //Solo per motivi di simulazione:
				            if(actuatorId.contentEquals("Act1")) {
				            	//System.out.println("Publish di INIZIO simulazione a Act1");
				            	mqtt.publish("{\"event\":\"start\"}", "sensorId_TempHum1");
				            }
				            else if(actuatorId.contentEquals("Act2")) {
				            	//System.out.println("Publish di INIZIO simulazione a Act2");
				            	mqtt.publish("{\"event\":\"start\"}", "sensorId_Temp2");
				            	mqtt.publish("{\"event\":\"start\"}", "sensorId_Hum2");
				            }
				            	
				            
				            
			            }
			            else if(actuatorState == 0) {
			            	db.UpdActState(actuatorId, 0);
				            
				            //Solo per motivi di simulazione:
				            if(actuatorId.contentEquals("Act1")) {
				            	//System.out.println("Publish di TERMINE simulazione a Act1");
				            	mqtt.publish("{\"event\":\"end\"}", "sensorId_TempHum1");
				            }
				            else if(actuatorId.contentEquals("Act2")) {
				            	//System.out.println("Publish di TERMINE simulazione a Act2");
				            	mqtt.publish("{\"event\":\"end\"}", "sensorId_Temp2");
				            	mqtt.publish("{\"event\":\"end\"}", "sensorId_Hum2");
				            }
				            	
			            }
			            else {
			            	 System.out.println("Valore pacchetto non riconosciuto.");
			            }
			            
			        }
			        //@Override
			        public void onError(){
			            System.err.println("Failed.................");
			        }
			    }

			);
	}
	
}


class MqttFactory{
	private MqttFactory() {}
	private static MqttCollector mqttInstance = null;
	
	
   public static MqttCollector createMqttManager() {
        if(mqttInstance == null)
        	MqttFactory.mqttInstance = new MqttCollector();
        return MqttFactory.mqttInstance;
    }

    public static MqttCollector getDbInstance(){
        return MqttFactory.mqttInstance;
    }
}



class MqttCollector implements MqttCallback{
	
	private static int irrigationSessionCount;
	private String topic1 = "temperature";
	private String topic2 = "humidity";
	private String topic3 = "sensorId_TempHum1"; //publish topic for simulation purpose
	private String topic4 = "sensorId_Temp2"; //publish topic for simulation purpose
	private String topic5 = "sensorId_Hum2"; //publish topic for simulation purpose
    private String broker = "tcp://127.0.0.1:1883";
    private String clientId = "MqttCollector";
    private boolean firstTime = true;
    private MqttClient mqttClient = null;
    private DBManager db = DBFactory.getDbInstance();
    private SimulationTime smt = SimulationTimeFactory.getDbInstance();
    
    public MqttCollector() {
    	 try {
    		
    		 //System.out.println(db.getClass().getSimpleName());
             mqttClient = new MqttClient(broker, clientId);
             System.out.println("Connecting to broker: " + broker);
             mqttClient.setCallback(this);
             mqttClient.connect();
             mqttClient.subscribe(topic1);
             mqttClient.subscribe(topic2);
         } catch (MqttException me) {
             me.printStackTrace();
         }
    }
    
 
    
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost cause: " + cause.getCause().getMessage());
    }
    
    public void disconnect()  {
    	try {
        mqttClient.disconnect();
    	}catch (MqttException me) {
         me.printStackTrace();
    	}
    }
    
    public void publish(String content, String node){
        try{
            MqttMessage message = new MqttMessage(content.getBytes());
            //this.mqttClient.publish("actuator_field2" + node, message);
            this.mqttClient.publish(node,message);
        }catch(MqttException me){
            me.printStackTrace();
        }
    }
    
    public void messageArrived(String topic, MqttMessage message) {
    	System.out.println(String.format("[%s] %s", topic, new String(message.getPayload())));
    	JSONObject responseText = new JSONObject(new String(message.getPayload()));
    	
    	
    	//String timest = Timestamp.from(Instant.now()).toString();
    	String timest = smt.getCurrentSimulatedDateTime();
    	//System.out.println("INFO:simulatedTimeGot:" + timest);
    	String sensorId = responseText.getString("sensorId");
    	//System.out.println("INFO:sensorId ottenuto:" + sensorId);
    	if(responseText.has("temperature")) {
    		double temperature = responseText.getDouble("temperature");
    		//System.out.println("INFO: Valore ottenuto:" + temperature);
    		db.RegNewTempObs(sensorId, timest, temperature);	
    	}
		else if(responseText.has("humidity")) {
			double humidity = responseText.getDouble("humidity");
			//System.out.println("INFO: Valore ottenuto:" + humidity);
			db.RegNewMWCObs(sensorId, timest, humidity);
		}
    	
    	
    	 //Business Logic:
    	 Actuator actuatorIdObject = db.GetBindAct(sensorId);
    	 String actuatorId = actuatorIdObject.getResourceName();
    	 int stateActuator = db.GetActState(actuatorId);
    	 //CalcolaMediaTemperatura;
    	 int last = 1;
    	 
    	 double temperature = 0.0;
    	 double humidity = 0.0;
    
    	 if(actuatorId.contentEquals("Act1")){
    	  temperature = db.GetLastnTempObs(sensorId, last);
    	 //CalcolaMediahumidity;
    	 //System.out.println("INFO: getLastTemp:"+ temperature);
    	  humidity = db.GetLastnMWCObs(sensorId, last);
    	 //System.out.println("INFO: getLastHum:"+ humidity);
    		}
    	else {
       	  temperature = db.GetLastnTempObs("Temp2", last);
       	 //CalcolaMediahumidity;
       	 //System.out.println("INFO: getLastTemp2:"+ temperature);
       	  humidity = db.GetLastnMWCObs("Hum2", last);
       	 //System.out.println("INFO: getLastHum2:"+ humidity);
       		}
    	 
    	 //DEBUG
    	 if(firstTime) {
    		 temperature = 25.0;
    		 humidity = 20.0;
    	 }
    	 firstTime = false;
    	 	
    	 
    	 
    	 //System.out.println("INFO: ActuatorId:"+ actuatorId + " + è"+ stateActuator);
    	 if(db.GetActState(actuatorId) != 1){
    		 //System.out.println("INFO: ActuatorId:"+ actuatorId + " + non è attivo");
    	 	//if(interval time){
    	 		if((temperature > 30 || humidity < 15) && (irrigationSessionCount < 3)){
    	 			db.UpdActState(actuatorId, 1);
    	 			//irrigationSessionCount++;
    	 			System.out.println("Irrigazione automatica in avviamento... " + actuatorId +"\n");
    	 			
    	 			//System.out.println("INFO: Mando un post all'attuatore\n");
    	 			//Preparo pacchetto CoAP 
    	 			String ipAddress = actuatorIdObject.getNodeAddress();

    	 			Request req = new Request(Code.POST);
    	 			String payload = "{\"mode\": on}";
    	 		    req.setPayload(payload);
    	 		    req.setURI("coap://["+ipAddress+"]/sensors/actuator");
    	 		    req.send();
    	 		    
    	 			//this.publish("{\"event\":\"start\"}",topic3);
        			//publish(payload, "simulation"); deve essere fatto in lato coap
    	 		}
    	 	
    	 	}
    	 
    	 }//endMessageArrived()
    	 

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Delivery completed");
    }
}





class SimulationTimeFactory{
	private SimulationTimeFactory() {}
	private static SimulationTime simulationTimeInstance = null;
	
	
   public static SimulationTime createSimulationTimeManager(int h,int m,int s,Duration realDurationTime,Duration simulatedDurationTime) {
        if(simulationTimeInstance == null)
        	SimulationTimeFactory.simulationTimeInstance = new SimulationTime(h,m,s,realDurationTime,simulatedDurationTime);
        return SimulationTimeFactory.simulationTimeInstance;
    }

    public static SimulationTime getDbInstance(){
        return SimulationTimeFactory.simulationTimeInstance;
    }
}



class SimulationTime {
	private long startTimer;
	private long observeTimer;
	private long timeElapsed;
	private LocalDateTime startRealTime;
	private Duration realDTime;
	private Duration simDTime;
	private DateTimeFormatter formatter;
	private long scaleTime;
	private boolean runningSimulation = false;

public SimulationTime(int h,int m,int s,Duration realDurationTime,Duration simulatedDurationTime){
	formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");// "yyyy-MM-dd HH:mm:ss"
	startRealTime = LocalDateTime.of(2022, Month.SEPTEMBER, 14,h,m,s); 
	this.realDTime = realDurationTime; //10 hours
	this.simDTime = simulatedDurationTime; //5 minutes
	scaleTime = realDTime.getSeconds() / simDTime.getSeconds();

}

public void startSimulationTimer(){
	startTimer = System.currentTimeMillis();
	runningSimulation = true;
}

public String getCurrentSimulatedDateTime(){
observeTimer = System.currentTimeMillis();
timeElapsed = (observeTimer - startTimer);///1000;
if(timeElapsed < simDTime.toMillis()){
	LocalDateTime tmpDataTime = startRealTime.plus(Duration.ofMillis(timeElapsed*scaleTime));
	String timeStamp = tmpDataTime.format(formatter);
	//System.out.println("DataTime: "+timeStamp); for DEBUGGING
	return timeStamp;
}
else{
System.out.println("Simulazione Terminata");
runningSimulation = false;
}
return "";

}

public boolean isRunningSimulation(){
return runningSimulation;
}



}






