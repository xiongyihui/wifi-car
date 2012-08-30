

#define MOTOR_NUM             4

#define MIN_RADIO             120
#define FIX_RADIO             240
#define START_RADIO           200
#define MAX_RADIO             255
#define RADIO_STEP            40

#define STOP_STATE            0
#define FORWARD_STATE         1
#define BACKWARD_STATE        2
#define TURN_LEFT_STATE       3
#define TURN_RIGHT_STATE      4

int pwmPin[MOTOR_NUM] = {
  5, 3, 9, 6};
int dirPin[MOTOR_NUM] = {
  4, 2, 8, 7};

int radio = START_RADIO;
int dir   = 0;
int state = 0;

char start_code[] = "GoGoGo";


void carForward()
{
#if 1
  int i;

  dir = 0;
  
  if (radio < FIX_RADIO)
  {
    for (i = 0; i < MOTOR_NUM; i++)
    {
      digitalWrite(dirPin[i], LOW);
      analogWrite(pwmPin[i], FIX_RADIO);
    }
    
    delay(100);
  }
  
  for (i = 0; i < MOTOR_NUM; i++)
  {
    digitalWrite(dirPin[i], LOW);
    analogWrite(pwmPin[i], radio);
  }
#else

  for (int i = 0; i < MOTOR_NUM; i++)
  {
    digitalWrite(dirPin[i], LOW);
    digitalWrite(pwmPin[i], HIGH);
  }
#endif
}

void carBackward()
{
  int i;

  dir = 1;
  for (i = 0; i < MOTOR_NUM; i++)
  {
    digitalWrite(dirPin[i], HIGH);
    analogWrite(pwmPin[i], MAX_RADIO - radio);
  }
}

void carStop()
{
  int i;

  for (i = 0; i < 4; i++)
  {
    digitalWrite(dirPin[i], LOW);
    digitalWrite(pwmPin[i], LOW);
  }
}

void carTurnLeft()
{
  digitalWrite(dirPin[0], HIGH);
  analogWrite(pwmPin[0], MAX_RADIO / 2);
  
  digitalWrite(dirPin[1], LOW);
  analogWrite(pwmPin[1], MAX_RADIO);
  
  digitalWrite(dirPin[2], LOW);
  analogWrite(pwmPin[2], 0);
  
  digitalWrite(dirPin[3], LOW);
  analogWrite(pwmPin[3], MAX_RADIO);
}

void carTurnRight()
{
  digitalWrite(dirPin[0], LOW);
  digitalWrite(pwmPin[0], HIGH);
  
  digitalWrite(dirPin[1], HIGH);
  analogWrite(pwmPin[1], MAX_RADIO / 2);
  
  digitalWrite(dirPin[2], LOW);
  digitalWrite(pwmPin[2], HIGH);
  
  digitalWrite(dirPin[3], LOW);
  digitalWrite(pwmPin[3], LOW);
}

void carAccelerate()
{
  int i;

  radio += RADIO_STEP;
  if (radio > MAX_RADIO)
  {
    radio = MAX_RADIO;
  }

  if (state == FORWARD_STATE)
  {
    for (i = 0; i < MOTOR_NUM; i++)
    {
      analogWrite(pwmPin[i], radio);
    }
  }
  else if (state == BACKWARD_STATE)
  {
    for (i = 0; i < MOTOR_NUM; i++)
    {
      analogWrite(pwmPin[i], MAX_RADIO - radio);
    }
  }
}

void carDecelerate()
{
  int i;

  radio -= RADIO_STEP;
  if (radio < MIN_RADIO)
  {
    radio = MIN_RADIO;
  }

  if (state == FORWARD_STATE)
  {
    for (i = 0; i < MOTOR_NUM; i++)
    {
      analogWrite(pwmPin[i], radio);
    }
  }
  else if (state == BACKWARD_STATE)
  {
    for (i = 0; i < MOTOR_NUM; i++)
    {
      analogWrite(pwmPin[i], 255 - radio);
    }
  }
}

void setup()
{
  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);
  
  for (int i = 0; i < MOTOR_NUM; i++)
  {
    pinMode(pwmPin[i], OUTPUT);
    pinMode(dirPin[i], OUTPUT);

    digitalWrite(pwmPin[i], LOW);
    digitalWrite(dirPin[i], LOW);
  }
  
  Serial.begin(115200);
  Serial.println("-----------Smart Car --------------\r\n");
  
  int j = 0;
  while (1) {
    if (Serial.available()) {
      if (Serial.read() == start_code[j])
      {
        j++;
        if (j == (4))
        {
          return;
        }
      }
      else {
        j = 0;
      }
    }
  }
  
  digitalWrite(13, LOW);
}

void loop()
{
  if (Serial.available())
  {
    digitalWrite(13, HIGH);
    
    int r = Serial.read();

    switch (r)
    {
    case 'w':
    case 'W':
      state = FORWARD_STATE;
      carForward();
      break;
    case 's':
    case 'S':
      state = BACKWARD_STATE;
      carBackward();
      break;
    case 'a':
    case 'A':
      state = TURN_LEFT_STATE;
      carTurnLeft();
      break;
    case 'd':
    case 'D':
      state = TURN_RIGHT_STATE;
      carTurnRight();
    case 'e':
    case 'E':
      carAccelerate();
      break;
    case 'c':
    case 'C':
      carDecelerate();
      break;
    case 'z':
    case 'Z':
      state = STOP_STATE;
      carStop();
      break;
    default:
      ;
    }
    
    digitalWrite(13, LOW);
  }
}



