file-tailer
==================

File Tailer is a daemon process that can runs on any machine and perform tailing of set of local files. 
As soon as a new record is being appended to the end of a file that the daemon is monitoring, it will send it to a Stream via REST API.

## Features

 - distributed as debian and rpm packages;
 - loads properties from configuration file;
 - supports rotation of log files;
 - persists state and is able to resume from first unsent record;
 - dumps statistics info;

## Usage

 In order to install File Tailer one should execute following command:
 
 - on Debian/Ubuntu systems:
 
 ```
    sudo apt-get install file-tailer.deb
 ```
 
 - on RHEL/CentOS systems:
 
 ```
    sudo rpm -ivh --force file-tailer.rpm
 ```
 

 Once installed, 
 To configure the daemon, edit following file:
 
 ```
    /etc/file-tailer/conf/file-tailer.properties
 ```
 
 At least following parameters should be specified:
 TODO: Add list of mandatory properties
 
 Please note that target file should be accessible for file-tailer user.
 In order to check this, one can use following command:
 
 ``` 
 sudo -u file-tailer more path_to_target_file
 ```
    
 Once configured, 
 To start the daemon, execute following command:
 
 ```
    sudo service file-tailer start
 ```
 
 To stop the daemon, execute following command:
 
 ```
    sudo service file-tailer stop
 ``` 
 
 File Tailer stores logs in /var/log/file-tailer folder.
 File Tailer stores pid, states and statistics in /var/run/file-tailer folder.
 
  
## Example Configuration
 
 Following configuration file will force file-tailer application to monitor two applications.
 Logs for each application will be sent to separate streams.
 
 ```
 
     # Comma-separated list of flows to be configured
     flows=app1Flow,app2Flow
     # General Flow properties
     
     # Flow source properties
     # Working directory (where to monitor files)
     flows.app1Flow.source.work_dir=/var/log/app1
     # Name of log file
     flows.app1Flow.source.file_name=app1.log
     
     # Flow sink properties
     # Name of the stream
     flows.app1Flow.sink.stream_name=app1Stream
     # Host name that is used by stream client
     flows.app1Flow.sink.host=reactor_host
     # Host port that is used by stream client
     flows.app1Flow.sink.port=10000
     
     # Flow source properties
     # Working directory (where to monitor files)
     flows.app2Flow.source.work_dir=/var/log/app2
     # Name of log file
     flows.app2Flow.source.file_name=app2.log
      
     # Flow sink properties
     # Name of the stream
     flows.app2Flow.sink.stream_name=app1Stream
     # Host name that is used by stream client
     flows.app2Flow.sink.host=reactor_host
     # Host port that is used by stream client
     flows.app2Flow.sink.port=10000

 ```
 

## Additional Notes
 
 TODO: Add description of all configuration parameters here 
