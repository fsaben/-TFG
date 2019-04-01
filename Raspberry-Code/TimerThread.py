import RPi.GPIO as GPIO  
import datetime
import time

high = []
low = []
        
def my_callback(channel):
    global high, low

    if GPIO.input(8) == GPIO.HIGH:
        high.append(str(datetime.datetime.now()))
    else:
        low.append(str(datetime.datetime.now()))
        
try:
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(12, GPIO.OUT)
    GPIO.setup(8, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
    GPIO.add_event_detect(8, GPIO.BOTH, callback=my_callback)
    
    led = GPIO.PWM(12, 200)
    print ("Incio\n")
    led.start(0)
    led.ChangeDutyCycle(50)
    
    message = raw_input('\nPress any key to exit.\n')

    """for i in range(0, 100, 1):
            led.ChangeDutyCycle(i)
            time.sleep(1)
            if (i == 100):
                i = 0"""

finally:
    GPIO.cleanup()

    print ("HIGH\n")
    for i in range(len(high)):
        print(high[i])
        
    print ("LOW\n")
    for i in range(len(low)):
        print(low[i])
        
    print("Goodbye!")
