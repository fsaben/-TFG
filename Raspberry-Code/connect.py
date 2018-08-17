import bluetooth

server = bluetooth.BluetoothSocket(bluetooth.RFCOMM)

print("Server created")
port = 0
server.bind(("", port))
print("Begin listening...")
server.listen(1)

client, address = server.accept()
print("Accepted connection from " + address)

data = client.recv(1024)
print("Recived [%s]" % data)

client.close()
server.close()
