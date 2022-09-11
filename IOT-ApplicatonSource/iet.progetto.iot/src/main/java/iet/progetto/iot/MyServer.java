package iet.progetto.iot;



import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

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
	private static String username = "JavaCollector";
	private static String password = "password";
	
	private static DBManager db = null;
	
	public static void main(String[] args) {
		
		//DBManager db = new DBManager("jdbc:mysql://localhost/smart_irrigation_db_nuovo","JavaCollector","password");

		db =  DBFactory.createDBManager(url,username,password);
		

		MyServer server = new MyServer();
		//server.add(new CoAPResourceExample("Hello"));
		//server.start();
		
		//runCoapCollector();
	    runMqttCollector();
	    showOperations();
	    /*
	    while(true){
	        try {
					Integer selectedOperation = insertInputLine();
					Integer index;

					switch (selectedOperation) {
					case 0:
						//showResources();
						break;
					case 1:

						break;
					case 2:

						break;
					case 3:
						//showResourcesInformation();
						break;
					case 4:

						break;
					case 5:

						break;
					case 6:
						server.destroy();
						System.exit(0);
						break;
					default:
						showOperations();
						break;
					}

				} catch (Exception e) {
					System.out.println("Invalid input. Try Again\n");
					showOperations();
					e.printStackTrace();
				}
	    }
		
		*/
	    
		CoapObserver obs1 = new CoapObserver("Act1","fd00::212:4b00:f82:8a06");
		obs1.startObserving();
		//CoapObserver obs2 = new CoapObserver("Act2","fd00::");
		//*********************
		while(true);
	}

    public static void runMqttCollector(){
        new Thread() {
            public void run(){
                new MqttCollector();
            }
        }.start();
    }
    
    /*
    public static void runCoapCollector(){
        new Thread() {
            public void run(){
                CoapCollector coll = new CoapCollector();
                coll.startServer();
            }
        }.start();
    }*/

    public static void showOperations() {
		System.out.println("Commands List:");
		System.out.println("0: show resources");
		System.out.println("1: start irrigation");
		System.out.println("2: stop irrigation");
		System.out.println("3: nodes status");
		System.out.println("4: show last humidity value");
		System.out.println("5: show single resource");
		System.out.println("6: exit");
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

	/* WORKS
	CoapClient client = new CoapClient("coap://[fd00::202:2:2:2]/sensors/temperature");
	CoapObserveRelation relation = client.observe(
	    new CoapHandler(){
	    	//@Override
	        public void onLoad(CoapResponse response){
	            String content = response.getResponseText();
	            //Logica per gestire l'informazione ricevuta
	            //inserimento in db
	            System.out.println(content);
	        }
	        //@Override
	        public void onError(){
	            System.err.println("Failed.................");
	        }
	    }

	);
	try {
	    Thread.sleep(6*1000);
	}catch(InterruptedException e){}
	*/
	//* ********************
    /*
	System.out.print("Invio pacchetto post al sensore\n");
	CoapClient client = new CoapClient("coap://[fd00::203:3:3:3]/sensors/actuator");
	Request req = new Request(Code.POST);
	String payload = "mode=on";
    req.setPayload(payload);
    req.setURI("coap://[" + "fd00::203:3:3:3" +"]/" + "sensors/actuator");
    req.send();
    
	*/
    
    
    //server.destroy();



//Rinominare coapObserver
class CoapObserver {
	private CoapClient client;
	private DBManager db = DBFactory.getDbInstance();
	private boolean notWorking = false;
	private String actuatorId;
	
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
			            if(actuatorState == 1) {
				            String timest = Timestamp.from(Instant.now()).toString();
				            db.RegActuatorActivation(actuatorId, timest);
			            }
			            else if(actuatorState == 0) {
			            	db.UpdActState(actuatorId, 0);
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






class MqttCollector implements MqttCallback{
	

	private String topic1 = "temperature";
	private String topic2 = "humidity";
	private String topic3 = "sensorId5"; //publish topic for simulation purpose
	private String topic4 = "sensorId5"; //publish topic for simulation purpose
	private String topic5 = "sensorId5"; //publish topic for simulation purpose
    private String broker = "tcp://127.0.0.1:1883";
    private String clientId = "MqttCollector";
    private MqttClient mqttClient = null;
    private DBManager db = DBFactory.getDbInstance();
    
    public MqttCollector() {
    	 try {
    		 
    		 System.out.println(db.getClass().getSimpleName());
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
    	
    	
    	String timest = Timestamp.from(Instant.now()).toString();
    	String sensorId = responseText.getString("sensorId");
    	System.out.println("INFO:sensorId ottenuto:" + sensorId);
    	if(responseText.has("temperature")) {
    		double temperature = responseText.getDouble("temperature");
    		System.out.println("INFO: Valore ottenuto:" + temperature);
    		db.RegNewTempObs(sensorId, timest, temperature);	
    	}
		else if(responseText.has("humidity")) {
			double humidity = responseText.getDouble("humidity");
			System.out.println("INFO: Valore ottenuto:" + humidity);
			db.RegNewMWCObs(sensorId, timest, humidity);
		}
    	
    	
    	 //Business Logic:
    	 Actuator actuatorIdObject = db.GetBindAct(sensorId);
    	 String actuatorId = actuatorIdObject.getResourceName();
    	 int stateActuator = db.GetActState(actuatorId);
    	 //CalcolaMediaTemperatura;
    	 int last = 1;
    	 double temperature = db.GetLastnTempObs(actuatorId, last);
    	 //CalcolaMediahumidity;
    	 double humidity = db.GetLastnMWCObs(actuatorId, last);
    	 
    	 
    	 System.out.println("INFO: ActuatorId:"+ actuatorId + " + è"+ stateActuator);
    	 if(db.GetActState(actuatorId) != 1){
    		 System.out.println("INFO: ActuatorId:"+ actuatorId + " + non è attivo");
    	 	//if(interval time){
    	 		if(temperature > 30 || humidity < 15){
    	 			System.out.println("INFO: Irrigazione automatica in avviamento...\n");
    	 			System.out.println("INFO: Dovrei mandare un post all'attuatore\n");
    	 			//Preparo pacchetto CoAP 
    	 			String ipAddress = actuatorIdObject.getNodeAddress();
    	 			//CoapClient client = new CoapClient("coap://["+ipAddress+"]/sensors/actuator");
    	 			System.out.println("IpAddress:"+ ipAddress);
    	 			Request req = new Request(Code.POST);
    	 			String payload = "{\"mode\": on}";
    	 		    req.setPayload(payload);
    	 		    req.setURI("coap://["+ipAddress+"]/sensors/actuator");
    	 		    req.send();
    	 			
        			//publish(payload, "simulation"); deve essere fatto in lato coap
    	 		}
    	 	
    	 	}
    	 
    	 }//endMessageArrived()
    	 

        
    
    
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Delivery completed");
    }
}
