%ECHO OFF
%ECHO Starting ECS And Security System
PAUSE
%ECHO Starting Temperature Controller Console
START "TEMPERATURE CONTROLLER CONSOLE" /MIN /NORMAL java TemperatureController %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY CONTROLLER CONSOLE" /MIN /NORMAL java HumidityController %1
START "TEMPERATURE SENSOR CONSOLE" /MIN /NORMAL java TemperatureSensor %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY SENSOR CONSOLE" /MIN /NORMAL java HumiditySensor %1
%ECHO ECS Monitoring Console
START "MUSEUM ENVIRONMENTAL CONTROL SYSTEM CONSOLE" /NORMAL java ECSConsole %1
%ECHO Starting Security Controller Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java SecurityController %1
%ECHO Starting Security Simulation Controller Console
START "SECURITY SIMULATION CONTROLLER CONSOLE" /MIN /NORMAL java SecuritySimulationConsole%1
%ECHO Starting Window Sensor Console
START "WINDOW SENSOR CONSOLE" /MIN /NORMAL java WindowSensor %1
%ECHO Starting Door Sensor Console
START "DOOR SENSOR CONSOLE" /MIN /NORMAL java DoorSensor %1
%ECHO Starting Motion Sensor Console
START "MOTION SENSOR CONSOLE" /MIN /NORMAL java MotionSensor %1
%ECHO ECS Monitoring Console
START "MUSEUM SECURITY CONTROL SYSTEM CONSOLE" /NORMAL java SecurityConsole %1