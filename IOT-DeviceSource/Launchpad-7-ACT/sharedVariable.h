



#include "sys/etimer.h"
#include "sys/process.h"
bool actuator_state;//con static non funzionava, creava due variabili actuator_state per ogni file.c.... mah
bool irrigazione_automatica;
struct etimer timer_irrigation;//non funzionava con static
struct etimer timer_leds;//con static gli indirizzi sono duplicati
//PROCESS_NAME(er_example_server);

