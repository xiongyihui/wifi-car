
#ifndef __WIFICAR_H
#define __WIFICAR_H

#if ARDUINO >= 100
 #include "Arduino.h"
#else
 #include "WProgram.h"
#endif

#include "wiring_private.h"
#include "Servo.h"

#ifndef __TYPE_REDEFINE
#define __TYPE_REDEFINE
typedef uint8_t u8;
typedef int8_t  s8;
typedef uint16_t u16;
typedef int16_t  s16;
typedef uint32_t u32;
typedef int32_t  s32;
#endif

#define WIFICAR_LDIR_N						7 //arduino pin number
#define WIFICAR_LDIR_P						8 //arduino pin number
#define WIFICAR_LSPEED						11//arduino pin number
#define WIFICAR_LPWM_ON()					sbi(TCCR2A, COM2A1);//OC2A
#define WIFICAR_LPWM_OFF()					cbi(TCCR2A, COM2A1);
#define WIFICAR_LSPEED_REG					OCR2A

#define WIFICAR_RDIR_N						2 //arduino pin number
#define WIFICAR_RDIR_P						4 //arduino pin number
#define WIFICAR_RSPEED						3//arduino pin number
#define WIFICAR_RPWM_ON()					sbi(TCCR2A, COM2B1);//OC2B
#define WIFICAR_RPWM_OFF()					cbi(TCCR2A, COM2B1);
#define WIFICAR_RSPEED_REG					OCR2B

#define WIFICAR_SPEED_MAX                   230
#define WIFICAR_SPEED_MIN                   85
#define WIFICAR_SPEED_INIT                  ((WIFICAR_SPEED_MAX+WIFICAR_SPEED_MIN)/2)
#define WIFICAR_SPEED_INC                   5
#define WIFICAR_SPEED_TURN_SLOW             120
#define WIFICAR_SPEED_TURN_FAST             200

#define WIFICAR_STA_STOP                    0
#define WIFICAR_STA_FORWARD                 1
#define WIFICAR_STA_BACKWARD                2
#define WIFICAR_STA_TURN_LEFT               3
#define WIFICAR_STA_TURN_RIGHT              4

#define WIFICAR_LED							13
#define WIFICAR_LED_OUTPUT()				pinMode(WIFICAR_LED, OUTPUT);
#define WIFICAR_LED_ON()					digitalWrite(WIFICAR_LED, HIGH)
#define WIFICAR_LED_OFF()					digitalWrite(WIFICAR_LED, LOW)
#define WIFICAR_LED_V()						digitalWrite(WIFICAR_LED, \
											(digitalRead(WIFICAR_LED)==HIGH)? LOW : HIGH)

#define WIFICAR_CAM_XY                      5 //arduino pin number
#define WIFICAR_CAM_XY_ANGLE_MAX            175
#define WIFICAR_CAM_XY_ANGLE_MIN            5
#define WIFICAR_CAM_XY_ANGLE_INC            3

#define WIFICAR_CAM_YZ                      6 //arduino pin number
#define WIFICAR_CAM_YZ_ANGLE_MAX            160
#define WIFICAR_CAM_YZ_ANGLE_MIN            0
#define WIFICAR_CAM_YZ_ANGLE_INC            3

typedef enum{
	LED_STA_OFF, LED_STA_ON, LED_STA_BLK, LED_STA_BLK_QUICK, LED_STA_BLK_VERY_QUICK
}led_sta_t;

class WIFICAR{
public:
	WIFICAR(void);
	void begin();
	void forward(u8 v);
	void backward(u8 v);
	void forward();
	void backward();
	void accelerate();
	void decelerate();
	void turn(u8 ls, u8 rs);
	void turn_left();
	void turn_right();
	void stop();
	
	void check_speed();
	
	void cam_init();
	void cam_up();
	void cam_down();
	void cam_left();
	void cam_right();
	void cam_center();
	
	void led_process();
	void led_set(led_sta_t sta);
private:
	int velocity;
	u8 state;
	led_sta_t led_sta;
	unsigned long led_time, led_ms;
	
	Servo cam_xy;
	Servo cam_yz;
	int cam_xy_angle;
	int cam_yz_angle;
};

#endif