import RPi.GPIO as GPIO
import time

try:
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(12, GPIO.OUT)

    led = GPIO.PWM(12, 100)
    print ("Incio\n")
    led.start(0)

    while True:
        for i in range(0, 100, 1):
            led.ChangeDutyCycle(i)
            time.sleep(0.2)
            if (i == 100):
                i = 0
except:
    GPIO.cleanup()
    print ("END\n")
