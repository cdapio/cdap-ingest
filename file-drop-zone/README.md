File DropZone
==================

File DropZone is to support user easily perform bulk ingestion using local files.

## Features

 - distributed as debian and rpm packages;
 - loads properties from configuration file;
 - support multiple observers/topics
 - able to survive restart and resume sending from the first unsent record of each of the existing files.
 - cleanup files that are completely sent

## Usage

 In order to install File DropZone one should execute following command:
 
 - on Debian/Ubuntu systems:
 
 ```
    sudo apt-get install file-drop-zone.deb
 ```
 
 - on RHEL/CentOS systems:
 
 ```
    sudo rpm -ivh --force file-drop-zone.rpm
 ```
 

 Once installed, 
 To configure the daemon, edit following file:
 
 ```
    /etc/file-drop-zone/conf/file-drop-zone.properties
 ```
 
 At least following parameters should be specified:

  - polling_interval
  - observers
  - work_dir
  - observers.obs1.pipe
  - pipes.pipe1.sink.stream_name
  - pipes.pipe1.sink.host
  - pipes.pipe1.sink.port

 Once configured, 
 To start the daemon, execute following command:
 
 ```
    sudo service file-drop-zone start
 ```
 
 To stop the daemon, execute following command:
 
 ```
    sudo service file-drop-zone stop
 ``` 
 
 File Drop Zone stores logs in /var/log/file-drop-zone folder.
 File Drop Zone stores pid, states and statistics in /var/run/file-drop-zone folder.
 
  
## Example Configuration
 
 Following configuration file will force file-drop-zone application to observe 2 directories using 2 Pipes.
 
 ```
 
     # Polling directories interval in milliseconds
     polling_interval=5000

     # Comma-separated list of directories observers to be configured
     observers=obs1

     #Path to work directory
     work_dir=/var/file-drop-zone/

     # General observer configurations
     # Pipe is used for loading data from the file to the Stream
     observers.obs1.pipe=pipe1

     # Pipe sink properties
     # Name of the stream
     pipes.pipe1.sink.stream_name=logEventStream
     # Host name that is used by stream client
     pipes.pipe1.sink.host=localhost
     # Host port that is used by stream client
     pipes.pipe1.sink.port=10000

 ```
