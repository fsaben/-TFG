import RPi.GPIO as GPIO  
import datetime
import time
        
def my_callback(channel):  
    if GPIO.input(8) == GPIO.HIGH:
        print('\nAt ' + str(datetime.datetime.now()))
        if GPIO.input(7) == 0:
            GPIO.output(7, 1)  
            print('lights on')
            
            time.sleep(5) #Here is the delay in the script that does not produce the desired result
            GPIO.output(7, 0)
            print('lights off')
        
        elif GPIO.input(7) ==1:
            GPIO.output(7, 0)  
            print('lights off')
        
    else:
        print('\nAt ' + str(datetime.datetime.now()))
        print('button released')
        
try:
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(12, GPIO.OUT)
    GPIO.setup(7, GPIO.OUT)
    GPIO.setup(8, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
    GPIO.add_event_detect(8, GPIO.BOTH, callback=my_callback, bouncetime=500)
    
    led = GPIO.PWM(12, 100)
    print ("Incio\n")
    led.start(0)
    
    while True:
        for i in range(0, 100, 1):
            led.ChangeDutyCycle(i)
            time.sleep(1)
            if (i == 100):
                i = 0
finally:
    GPIO.cleanup()
    print("Goodbye!")
