import bluetooth

devices = bluetooth.discover_devices()

bt_addr = "24:1F:A0:D3:92:73"
port = 1


try:
    for device in devices:
        for _ in bluetooth.find_service(address=device):
            if 'RFCOMM' in _['protocol']:
               print("Address:  " + _['host'] + "\tPort:  " + str(_['port']) + "\tName:  " + bluetooth.lookup_name(device))

    for device in devices:
        for _ in bluetooth.find_service(address=device):
            if 'RFCOMM' in _['protocol'] and bt_addr == _['host']:
                port = _['port']
                break

except bluetooth.BluetoothError as e:
    print("\n\nCan't connect to device %s" % bluetooth.lookup_name(bt_addr))
    print("Error: " + str(e) + "\n\n")


try:
    sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    sock.connect((bt_addr, port))
    print("\n\nConected!")
    print("Connected to %s in port %s\n\n" % (bt_addr, port))
    sock.close()

except bluetooth.BluetoothError as e:
    print("\n\nCan't connect to device %s" % bluetooth.lookup_name(bt_addr))
    print("Error: " + str(e) + "\n\n")

