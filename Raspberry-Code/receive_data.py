import bluetooth
import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(3, GPIO.OUT)
GPIO.output(3, 0)

server = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
server.bind(("", 0))
server.listen(1)

client, address = server.accept()
print("Accepted connection from %s" % str(address))

while True:
    data = client.recv(1024).decode()
    print("Revived: %s" % data)

    if data == "0":
        print("LED off")
        GPIO.output(3, GPIO.LOW)
    if data == "1":
        print("LED on")
        GPIO.output(3, 1)
    if data == "q":
        print("Exit")
        break

client.close()
server.close()
