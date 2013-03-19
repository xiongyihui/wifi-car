#include "WifiCar.h"

WIFICAR :: WIFICAR()
{

}

void WIFICAR :: begin()
{
	velocity = WIFICAR_SPEED_INIT;
	state = WIFICAR_STA_STOP;
	led_sta = LED_STA_OFF;
	led_time = millis();

	pinMode(WIFICAR_LDIR_N, OUTPUT);
	pinMode(WIFICAR_LDIR_P, OUTPUT);
	pinMode(WIFICAR_LSPEED, OUTPUT);

	pinMode(WIFICAR_RDIR_N, OUTPUT);
	pinMode(WIFICAR_RDIR_P, OUTPUT);
	pinMode(WIFICAR_RSPEED, OUTPUT);

	/** car stop */
	digitalWrite(WIFICAR_LSPEED, LOW);
	digitalWrite(WIFICAR_LSPEED, LOW);

	WIFICAR_LED_OUTPUT();

	/** Timer2 PWM phase correct 8-bit, CLKio/8 */
	TCCR2A = 0;
	TCCR2B = 0;
	sbi(TCCR2A, WGM20);
	sbi(TCCR2B, CS20);

	cam_init();
}

void WIFICAR :: forward(u8 v)
{
	state = WIFICAR_STA_FORWARD;

	digitalWrite(WIFICAR_LDIR_P, LOW);
	digitalWrite(WIFICAR_LDIR_N, HIGH);

	digitalWrite(WIFICAR_RDIR_N, LOW);
	digitalWrite(WIFICAR_RDIR_P, HIGH);

	// analogWrite(LEFT_SPEED, v);
	// analogWrite(RIGHT_SPEED, v);
	WIFICAR_LSPEED_REG = v;
	WIFICAR_RSPEED_REG = v;
	WIFICAR_LPWM_ON();
	WIFICAR_RPWM_ON();
}

void WIFICAR :: backward(u8 v)
{
	state = WIFICAR_STA_BACKWARD;

	digitalWrite(WIFICAR_LDIR_N, LOW);
	digitalWrite(WIFICAR_LDIR_P, HIGH);

	digitalWrite(WIFICAR_RDIR_P, LOW);
	digitalWrite(WIFICAR_RDIR_N, HIGH);

	// analogWrite(LEFT_SPEED, v);
	// analogWrite(RIGHT_SPEED, v);
	WIFICAR_LSPEED_REG = v;
	WIFICAR_RSPEED_REG = v;
	WIFICAR_LPWM_ON();
	WIFICAR_RPWM_ON();
}

void WIFICAR :: forward()
{
	forward(velocity);
	check_speed();
}

void WIFICAR :: backward()
{
	backward(velocity);
	check_speed();
}


void WIFICAR :: accelerate()
{
	velocity += WIFICAR_SPEED_INC;
	if(velocity > WIFICAR_SPEED_MAX) {
		velocity = WIFICAR_SPEED_MAX;
	}
	check_speed();
	switch(state) {
	case WIFICAR_STA_FORWARD:
	case WIFICAR_STA_BACKWARD:
		WIFICAR_LSPEED_REG = velocity;
		WIFICAR_RSPEED_REG = velocity;
		break;
	}
}
void WIFICAR :: decelerate()
{
	velocity -= WIFICAR_SPEED_INC;
	if(velocity < WIFICAR_SPEED_MIN) {
		velocity = WIFICAR_SPEED_MIN;
	}
	check_speed();
	switch(state) {
	case WIFICAR_STA_FORWARD:
	case WIFICAR_STA_BACKWARD:
		WIFICAR_LSPEED_REG = velocity;
		WIFICAR_RSPEED_REG = velocity;
		break;
	}
}

void WIFICAR :: check_speed()
{
	if(velocity == WIFICAR_SPEED_MAX) {
		led_set(LED_STA_ON);
	} else if(velocity > ((2*WIFICAR_SPEED_MAX+WIFICAR_SPEED_MIN)/3)) {
		led_set(LED_STA_BLK_VERY_QUICK);
	} else if(velocity > ((WIFICAR_SPEED_MAX+2*WIFICAR_SPEED_MIN)/3)) {
		led_set(LED_STA_BLK_QUICK);
	} else if(velocity == WIFICAR_SPEED_MIN) {
		led_set(LED_STA_ON);
	} else {
		led_set(LED_STA_BLK);
	}
}

void WIFICAR :: turn(u8 ls, u8 rs)
{
	digitalWrite(WIFICAR_LDIR_P, LOW);
	digitalWrite(WIFICAR_LDIR_N, HIGH);

	digitalWrite(WIFICAR_RDIR_N, LOW);
	digitalWrite(WIFICAR_RDIR_P, HIGH);

	// analogWrite(LEFT_SPEED, ls);
	// analogWrite(RIGHT_SPEED, rs);
	WIFICAR_LSPEED_REG = ls;
	WIFICAR_RSPEED_REG = rs;
	WIFICAR_LPWM_ON();
	WIFICAR_RPWM_ON();
}

void WIFICAR :: turn_left()
{
	state = WIFICAR_STA_TURN_LEFT;
	turn(WIFICAR_SPEED_TURN_SLOW, WIFICAR_SPEED_TURN_FAST);
}

void WIFICAR :: turn_right()
{
	state = WIFICAR_STA_TURN_RIGHT;
	turn(WIFICAR_SPEED_TURN_FAST, WIFICAR_SPEED_TURN_SLOW);
}

void WIFICAR :: stop()
{
	/** car stop */
	state = WIFICAR_STA_STOP;

	WIFICAR_LPWM_OFF();
	WIFICAR_RPWM_OFF();
	digitalWrite(WIFICAR_LSPEED, LOW);
	digitalWrite(WIFICAR_RSPEED, LOW);

	led_set(LED_STA_OFF);
}

void WIFICAR :: led_set(led_sta_t sta)
{
	if(sta == led_sta) {
		return;
	}
	led_sta = sta;
	switch(sta) {
	case LED_STA_OFF:
		WIFICAR_LED_OFF();
		break;
	case LED_STA_ON:
		WIFICAR_LED_ON();
		break;
	case LED_STA_BLK:
	case LED_STA_BLK_QUICK:
	case LED_STA_BLK_VERY_QUICK:
		WIFICAR_LED_ON();
		led_time = millis();
		break;
	}
}

void WIFICAR :: led_process()
{
	switch(led_sta) {
	case LED_STA_OFF:
		break;
	case LED_STA_ON:
		break;
	case LED_STA_BLK:
		led_ms = millis();
		if(led_ms-led_time>500) {
			led_time = led_ms;
			WIFICAR_LED_V();
		}
		break;
	case LED_STA_BLK_QUICK:
		led_ms = millis();
		if(led_ms-led_time>200) {
			led_time = led_ms;
			WIFICAR_LED_V();
		}
		break;
	case LED_STA_BLK_VERY_QUICK:
		led_ms = millis();
		if(led_ms-led_time>50) {
			led_time = led_ms;
			WIFICAR_LED_V();
		}
		break;
	}

}

void WIFICAR :: cam_init()
{
	cam_xy.attach(WIFICAR_CAM_XY);
	cam_yz.attach(WIFICAR_CAM_YZ);

	cam_xy_angle = 90;
	cam_yz_angle = 0;

	cam_xy.write(cam_xy_angle);
	cam_yz.write(cam_yz_angle);
}

void WIFICAR :: cam_up()
{
	cam_yz_angle+=WIFICAR_CAM_YZ_ANGLE_INC;
	if(cam_yz_angle>WIFICAR_CAM_YZ_ANGLE_MAX) {
		cam_yz_angle = WIFICAR_CAM_YZ_ANGLE_MAX;
	}
	cam_yz.write(cam_yz_angle);
}

void WIFICAR :: cam_down()
{
	cam_yz_angle-=WIFICAR_CAM_YZ_ANGLE_INC;
	if(cam_yz_angle < WIFICAR_CAM_YZ_ANGLE_MIN) {
		cam_yz_angle = WIFICAR_CAM_YZ_ANGLE_MIN;
	}
	cam_yz.write(cam_yz_angle);
}

void WIFICAR :: cam_left()
{
	cam_xy_angle += WIFICAR_CAM_XY_ANGLE_INC;
	if(cam_xy_angle>WIFICAR_CAM_XY_ANGLE_MAX) {
		cam_xy_angle = WIFICAR_CAM_XY_ANGLE_MAX;
	}
	cam_xy.write(cam_xy_angle);
}

void WIFICAR :: cam_right()
{
	/** camera right */
	cam_xy_angle -= WIFICAR_CAM_XY_ANGLE_INC;
	if(cam_xy_angle < WIFICAR_CAM_YZ_ANGLE_MIN) {
		cam_xy_angle = WIFICAR_CAM_YZ_ANGLE_MIN;
	}
	cam_xy.write(cam_xy_angle);
}

void WIFICAR :: cam_center()
{
	cam_xy_angle = 90;
	cam_yz_angle = 0;
	cam_xy.write(cam_xy_angle);
	cam_yz.write(cam_yz_angle);
}




