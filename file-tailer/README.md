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

  - flows
  - flows.flow1.source.work_dir
  - flows.flow1.source.file_name
  - flows.flow1.source.rotated_file_name_pattern
  - flows.flow1.sink.stream_name
  - flows.flow1.sink.host
  - flows.flow1.sink.port
 
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
 
 Configuration parameters description:

 - daemon_dir - the path to directory, intended like storage for File Tailer state and metrics
 - flows - list of all flows
 - flows.<flow name>.name - name of this flow
 - flows.<flow name>.state_file - name of file, to which File Tailer save state
 - flows.<flow name>.statistics_file - name of file, to which File Tailer save statistics
 - flows.<flow name>.queue_size - size of queue, which intended to store logs before sending them to REST API
 - flows.<flow name>.source.work_dir - path to directory, where monitor log files
 - flows.<flow name>.source.file_name - name of log file
 - flows.<flow name>.source.rotated_file_name_pattern - log file rolling pattern
 - flows.<flow name>.source.charset_name - name of charset, used by Stream Client for sending logs
 - flows.<flow name>.source.record_separator - symbol, that separate each log record
 - flows.<flow name>.source.sleep_interval - interval to sleep, after read all log data
 - flows.<flow name>.source.failure_retry_limit - number of attempts to read logs, if occurred error while reading file data
 - flows.<flow name>.source.failure_sleep_interval - interval to sleep, if occurred error while reading file data
 - flows.<flow name>.sink.stream_name - name of target stream
 - flows.<flow name>.sink.host - server host
 - flows.<flow name>.sink.port - server port
 - flows.<flow name>.sink.ssl - Secure Socket Layer mode [true|false]
 - flows.<flow name>.sink.authToken - server security token
 - flows.<flow name>.sink.apiKey - ssl security key
 - flows.<flow name>.sink.writerPoolSize - number of threads, in which Stream Client sends events
 - flows.<flow name>.sink.version - reactor server version
 - flows.<flow name>.sink.packSize - number of logs sent at a time
 - flows.<flow name>.sink.failure_retry_limit - number of attempts to sent logs, if occurred error while reading file data
 - flows.<flow name>.sink.failure_sleep_interval - interval to sleep, if occurred error while sending logs
