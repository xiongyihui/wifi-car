#include "Servo.h"
#include "WifiCar.h"

WIFICAR myWifiCar;

void setup()
{
  Serial.begin(115200);

  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);

#ifdef __DEBUG
  Serial.println("-----------Smart Car --------------\r\n");
#endif

  /** wifi-car initial */
  myWifiCar.begin();

  digitalWrite(13, LOW);

  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  digitalWrite(5, LOW);
  digitalWrite(6, LOW);
}

void loop()
{
  unsigned char buf;
  /** led control process, must be polled in loop() */
  myWifiCar.led_process();

  if(Serial.available()){
    buf=Serial.read();
#ifdef __DEBUG
    Serial.write(buf);
#endif
    switch(buf){
    case 'w': 
    case 'W':
      /** forward */
      myWifiCar.forward();
      break;
    case 's': 
    case 'S':
      /** backward */
      myWifiCar.backward();
      break;
    case 'z': 
    case 'Z':
      /** stop */
      myWifiCar.stop();
      break;
    case 'e': 
    case 'E':
      /** accelerate */
      myWifiCar.accelerate();
      break;
    case 'c': 
    case 'C':
      /** decelerate */
      myWifiCar.decelerate();
      break;
    case 'a': 
    case 'A':
      /** turn left*/
      myWifiCar.turn_left();
      break;
    case 'd': 
    case 'D':
      /** turn right*/
      myWifiCar.turn_right();
      break;

      /** camera control */
    case 'i': 
    case 'I':
      /** camera up */
      myWifiCar.cam_up();
      break;
    case 'k': 
    case 'K':
      /** camera down */
      myWifiCar.cam_down();
      break;
    case 'j': 
    case 'J':
      /** camera left */
      myWifiCar.cam_left();
      break;
    case 'L': 
    case 'l':
      /** camera right */
      myWifiCar.cam_right();
      break;
    case 'O': 
    case 'o':
      /** center camera */
      myWifiCar.cam_center();
      break;

    }
  }
}

