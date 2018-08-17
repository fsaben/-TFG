import bluetooth
import time
import RPi.GPIO as GPIO

devices = bluetooth.discover_devices()
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

btAddr = "24:1F:A0:D3:92:73"
GPIO.setup(2, GPIO.OUT)
GPIO.setup(3, GPIO.OUT)

def print_available_devices():
    for device in devices:
        for _ in bluetooth.find_service(address=device):
            if 'RFCOMM' in _['protocol']:
                print("Address:  " + _['host'] + "\tPort:  " + str(_['port']) + "\tName:  " + bluetooth.lookup_name(device))

def connect(MacAddress):
    port = 1

    #look for the first available port for the given address
    for device in devices:
        for _ in bluetooth.find_service(address=device):
            if 'RFCOMM' in _['protocol'] and MacAddress == _['host']:
                port = _['port']
                break
    
    #try to connect
    try:
        sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        sock.connect((btAddr, port))
        print("\n\nConected!")
        print("Connected to %s in port %s\n\n" % (btAddr, port))
        sock.close()

    except bluetooth.BluetoothError as e:
        print("\n\nCan't connect to device %s" % bluetooth.lookup_name(btAddr))
        print("Error: " + str(e) + "\n\n")

def LedBlink():
    timeout = True
    count = 0
    while(timeout):
        GPIO.output(2, True)
        GPIO.output(3, False)
        time.sleep(0.2)

        GPIO.output(2, False)
        GPIO.output(3, True)
        time.sleep(0.2)

        count+=1
        if count == 20:
            timeout = False
    GPIO.cleanup()


connect(btAddr)
LedBlink()
