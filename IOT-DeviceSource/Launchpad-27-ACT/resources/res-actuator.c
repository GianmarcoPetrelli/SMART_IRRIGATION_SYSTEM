#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "../sharedVariable.h"
#include "os/dev/leds.h"
#include "contiki.h"

#include <time.h>
#include <stdint.h>
#include "sys/node-id.h"


/*
extern struct etimer timer_irrigation;
extern struct etimer timer_leds;
*/

//extern struct etimer timer_prova; 


/*Log configuration*/
#include "sys/log.h"
#define LOG_MODULE "ACTUATOR"
#define LOG_LEVEL LOG_LEVEL_INFO
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);



EVENT_RESOURCE(res_actuator,
			"title=\"Actuator\"mode=on|off;obs;rt=\"Text\"",
			res_get_handler,
			res_post_handler,
			NULL,
			NULL,
			res_event_handler);

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	
	printf("dentro get_handler actuator_state: %d\n",actuator_state);
	 coap_set_header_content_format(response, APPLICATION_JSON);
        sprintf((char *)buffer, "{\"state\": %d, \"mode\": \"prova\"}", actuator_state);
        coap_set_payload(response, buffer, strlen((char*)buffer));


}

static void res_event_handler(void){
printf("dentro event_handler actuator_state: %d\n",actuator_state);
    coap_notify_observers(&res_actuator);
}

static void
res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{

	
		LOG_INFO("Post request received\n");
		const uint8_t *payload = NULL;	
		//len = coap_get_post_variable(request, "mode", &mode);
		int len = coap_get_payload(request,&payload);
		printf("Payload: %s",(char *)payload);
		LOG_INFO("|%.*s",len, (char *)payload);
			
		actuator_state = true;
		irrigazione_automatica = true;
		/* BUG NON FUNZIONANO
		etimer_set(&timer_leds,CLOCK_SECOND*1);
		etimer_set(&timer_irrigation,CLOCK_SECOND*10); 
		leds_toggle(0);
		*/
}
