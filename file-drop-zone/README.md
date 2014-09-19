File DropZone
==================

File DropZone allows users to easily perform the bulk ingestion of local files.

## Features

 - Distributed as debian and rpm packages;
 - Loads properties from a configuration file;
 - Supports multiple observers/topics;
 - Able to survive restart and resumes sending from the first unsent record of each of the existing files; and
 - Removes files that are completely sent.

## Usage

 To install File DropZone, execute one of these commands:
 
 - on Debian/Ubuntu systems:
 
 ```
    sudo apt-get install file-drop-zone.deb
 ```
 
 - on RHEL/CentOS systems:
 
 ```
    sudo rpm -ivh --force file-drop-zone.rpm
 ```
 

 Once installed, configure the daemon by editing the file:
 
 ```
    /etc/file-drop-zone/conf/file-drop-zone.properties
 ```
 
 These parameters must be specified:

  - polling_interval
  - observers
  - work_dir
  - observers.obs1.pipe
  - pipes.pipe1.sink.stream_name
  - pipes.pipe1.sink.host
  - pipes.pipe1.sink.port

 To start the daemon, execute the command:
 
 ```
    sudo service file-drop-zone start
 ```
 
 To stop the daemon, execute the command:
 
 ```
    sudo service file-drop-zone stop
 ``` 
 
 File DropZone stores log files in the /var/log/file-drop-zone directory.
 PID, states and statistics are stored in the /var/run/file-drop-zone directory
 
## File uploading

  To upload the file, execute the command:
  
  ```
     file-drop-zone load <file-path> <observer>
  ```
  
  If only one observer is configured, the *observer* parameter is not required:
  
  ```
     file-drop-zone load <file-path>
  ```
  
## Authentication Client

 Once File DropZone is installed, configure the Authentication Client by editing the properties file:
 
 ```
    /etc/file-drop-zone/conf/auth-client.properties
 ```
 
 Authentication Client configuration parameters:
 
 - pipes.<pipe-name>.sink.auth_client - classpath of authentication client class
 - pipes.<pipe-name>.sink.auth_client_properties - path to authentication client properties file
 
## Authentication Client Example Configuration
 
 ```
 
     # User name
     security.auth.client.username=admin
     # User password
     security.auth.client.password=realtime
 ```
  
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
