#include<stdlib.h>
#include<Regexp.h>//for parsing sent data

int r_pin = 9;
int g_pin = 10;
int b_pin = 11;

int r;
int g; 
int b;

String bluetooth_data;
float brightness = 1;

//pwm control for rgb led
void RGB_color(int red_light_value, int green_light_value, int blue_light_value){
  analogWrite(r_pin, 255-(red_light_value*brightness));
  analogWrite(g_pin, 255-(green_light_value*brightness));
  analogWrite(b_pin, 255 - (blue_light_value*brightness));
}


void setup() {
  // put your setup code here, to run once:
  pinMode(r_pin, OUTPUT);
  pinMode(g_pin, OUTPUT);
  pinMode(b_pin, OUTPUT);
  
  Serial.begin(9600);
}

void loop() {
  while (Serial.available()){
    bluetooth_data = Serial.readStringUntil(')');
    int index = bluetooth_data.lastIndexOf("(");
    String col_string = bluetooth_data.substring(index+1);
    if (col_string[0] == '!'){
   
      int comma_pos = col_string.indexOf(',');
      r = col_string.substring(1,comma_pos).toInt();
      col_string = col_string.substring(comma_pos+1);
      comma_pos = col_string.indexOf(',');
      g = col_string.substring(0, comma_pos).toInt();
      col_string = col_string.substring(comma_pos+1);
      comma_pos = col_string.indexOf(',');
      b = col_string.substring(0, comma_pos).toInt();
      
      //Serial.println(g); 
      RGB_color(r,g,b);

      
    }
    else if (col_string[0] == '#'){
      float brightness_raw = col_string.substring(1).toInt();
      brightness = brightness_raw/100;
      RGB_color(r,g,b);
      //Serial.println(brightness);
    }
    
  }
}
