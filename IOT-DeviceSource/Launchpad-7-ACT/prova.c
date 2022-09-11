//funzionante, sarebbe da togliere un po' di printf e variabili actuator_state dal questo codice e da quello nella cartella resource


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "random.h"

#include "os/dev/button-hal.h"
#include "os/dev/leds.h"

#include "sharedVariable.h"



/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "ACTUATOR"
#define LOG_LEVEL LOG_LEVEL_APP



extern coap_resource_t res_actuator;

//static struct etimer timer_prova;
static struct etimer timer_prova;

PROCESS(er_example_server, "Actuator");
AUTOSTART_PROCESSES(&er_example_server);
PROCESS_THREAD(er_example_server, ev, data)
{
	PROCESS_BEGIN();
	
	coap_activate_resource(&res_actuator, "sensors/actuator");

  while(1){
		PROCESS_WAIT_EVENT();
		if(ev == PROCESS_EVENT_TIMER && data == &timer_prova){		
			printf("Sono entrato nel process_event_timer!!\n");
		}	
		
  }//endWhile                            

  PROCESS_END();
}
