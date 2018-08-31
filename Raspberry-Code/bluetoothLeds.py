import bluetooth
import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(3, GPIO.OUT)
GPIO.output(3, 0)

print("Start\n\n")

#Functions
def Leds():
    while True:
        data = client.recv(1024).decode()
        print("Revived: %s" % data)
        if data == "0":
            GPIO.output(3, 0)
            print("LED off")
        if data == "1":
            GPIO.output(3, 1)
            print("LED on")
        if data == "q":
            print("Bluetooth Disconnected\n\n")
            GPIO.output(3, 0)
            break

#Main Loop
while True:
    server = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    server.bind(("", 0))
    server.listen(1)

    try:
        client, address = server.accept()
        print("Accepted connection from %s" % str(address))
        Leds()

    except bluetooth.BluetoothError as e:
        print("Bluetooth not connected: [%s]\n\n" % str(e))

    client.close()
    server.close()
