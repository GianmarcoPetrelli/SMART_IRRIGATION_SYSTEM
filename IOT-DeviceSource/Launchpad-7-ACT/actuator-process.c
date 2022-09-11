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


#define DURATION_IRRIGATION 30
#define DURATION_LEDS_ILLUMINATION 1
static char* sensorId = "Act1";
extern coap_resource_t res_actuator;

static struct etimer e_timer;

bool buttonLeftClicked = false;



PROCESS(er_example_server, "Actuator");
AUTOSTART_PROCESSES(&er_example_server);

PROCESS_THREAD(er_example_server, ev, data)
{
	PROCESS_BEGIN();
	LOG_INFO("Starting Actuator Launchpad-15\n");
	//button_hal_button_t *button; //by button_event we can understan which button has been pressed
	actuator_state = false;
	leds_set(LEDS_NUM_TO_MASK(0));

	


	coap_activate_resource(&res_actuator, "sensors/actuator");
	etimer_set(&e_timer,0.5*CLOCK_SECOND);



  /* Define application-specific events here. */
  while(1) {
	PROCESS_WAIT_EVENT();

	if(irrigazione_automatica){
		irrigazione_automatica = false;
		actuator_state = true;
		res_actuator.trigger();
		etimer_set(&timer_leds,CLOCK_SECOND*DURATION_LEDS_ILLUMINATION);
		etimer_set(&timer_irrigation,CLOCK_SECOND*DURATION_IRRIGATION); 
	}

	if(ev == button_hal_periodic_event){
		button_hal_button_t *button = data;
		if(button->unique_id == BOARD_BUTTON_HAL_INDEX_KEY_LEFT){
			printf("Cliccato bottone 1/sinistro\n");
			printf("tempo %d\n",button->press_duration_seconds);
			if(button->press_duration_seconds > 3){
				//tempo di pressione bottone
				printf("tempo pressione sufficiente\n");
				if(actuator_state == true){
		    	printf("IRRIGAZIONE GIA IN CORSO\n");
		   		}
				else{
				actuator_state = true;
				res_actuator.trigger();
				etimer_set(&timer_leds,CLOCK_SECOND*DURATION_LEDS_ILLUMINATION);
				etimer_set(&timer_irrigation,CLOCK_SECOND*DURATION_IRRIGATION); 
				buttonLeftClicked = true;
				}
			}
		}
		else if(button->unique_id == BOARD_BUTTON_HAL_INDEX_KEY_RIGHT){
			printf("Cliccato bottone 2/destro\n");
			if(!buttonLeftClicked){
				printf("Nessuna irrigazione manuale in corso\n");
				if(actuator_state){
				printf("Irrigazione automatica non può essere interrotta manualmente\n");
        }
			}
			else{
			printf("Interruzione manuale della irrigazione\n");
			etimer_stop(&timer_leds);
			etimer_stop(&timer_irrigation);
			actuator_state=false;
			res_actuator.trigger();
			leds_set(LEDS_NUM_TO_MASK(0));	
			buttonLeftClicked = false;	
			}
		}
	}//endIfButtonEvent
	if(ev == PROCESS_EVENT_TIMER && data == &timer_irrigation){
		printf("Tempo di irrigazione terminato\n");
		etimer_stop(&timer_leds);
		//etimer_stop(&timer_irrigation); non necessario
		actuator_state=false;
		res_actuator.trigger();
		buttonLeftClicked = false;
		leds_set(LEDS_NUM_TO_MASK(0));
	}
	if(ev == PROCESS_EVENT_TIMER && data == &timer_leds){
		//printf("lucine effetto natale\n");
		leds_single_toggle(0); 
		leds_single_toggle(1); 
		etimer_set(&timer_leds,DURATION_LEDS_ILLUMINATION*CLOCK_SECOND);
	}//EndIfTimerEvent
	
	etimer_reset(&e_timer);
  }//endWhile                            

  PROCESS_END();
}
