EvDash runs on any wifi enabled Android device.  It is able to use data from a Soliton1 when the Soliton1 is connected to a wifi router.


The EVnetics Soliton1 is a Series Field DC Motor Controller for electric vehicles with an ethernet port for streaming performance data.

A cheap wifi router such as Linksys WRT54G is required.


Linksys settings:
Internet Connection Type: Static IP
Internet IP Address: 169.254.0.2
Subnet Mask: 255.255.0.0
Gateway: 169.254.0.1

Then I just used DHCP for the internal network to be the same as it normally is for my PC which is 192.168.1.100. The router address is 192.168.1.1 with subnet mask of 255.255.255.0

NB. This is not the only possible way to setup the router.

Dashboard Features for Soliton1:

The application includes the following gauges:
- Traction Pack Voltage
- Motor RPM
- Battery Amps 0-180A
- Battery Amps 0-1800A
- Controller Temp

Any combination of the gauges can be shown on screen in any position with various sizes. The status light in the top left hand corner shows if data is being received.

Grey light - not connected yet
Blue light - waiting for data
Green light - data is streaming
Red light - problem with connection


Other gauges include:

Speed (kph)
Speed (mph)
Old school speed dial (mph)
Location/Bearing data
Lateral G's
Acceleration G's
