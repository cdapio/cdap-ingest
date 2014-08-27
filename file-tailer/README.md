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

  - pipes
  - pipes.pipe1.source.work_dir
  - pipes.pipe1.source.file_name
  - pipes.pipe1.source.rotated_file_name_pattern
  - pipes.pipe1.sink.stream_name
  - pipes.pipe1.sink.host
  - pipes.pipe1.sink.port
 
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
 
     # Comma-separated list of pipes to be configured
     pipes=app1pipe,app2pipe
     # General pipe properties
     
     # pipe source properties
     # Working directory (where to monitor files)
     pipes.app1pipe.source.work_dir=/var/log/app1
     # Name of log file
     pipes.app1pipe.source.file_name=app1.log
     
     # pipe sink properties
     # Name of the stream
     pipes.app1pipe.sink.stream_name=app1Stream
     # Host name that is used by stream client
     pipes.app1pipe.sink.host=reactor_host
     # Host port that is used by stream client
     pipes.app1pipe.sink.port=10000
     
     # pipe source properties
     # Working directory (where to monitor files)
     pipes.app2pipe.source.work_dir=/var/log/app2
     # Name of log file
     pipes.app2pipe.source.file_name=app2.log
      
     # pipe sink properties
     # Name of the stream
     pipes.app2pipe.sink.stream_name=app1Stream
     # Host name that is used by stream client
     pipes.app2pipe.sink.host=reactor_host
     # Host port that is used by stream client
     pipes.app2pipe.sink.port=10000

 ```
 

## Additional Notes
 
 Configuration parameters description:

 - daemon_dir - the path to directory, intended like storage for File Tailer state and metrics
 - pipes - list of all pipes
 - pipes.<pipe name>.name - name of this pipe
 - pipes.<pipe name>.state_file - name of file, to which File Tailer save state
 - pipes.<pipe name>.statistics_file - name of file, to which File Tailer save statistics
 - pipes.<pipe name>.queue_size - size of queue, which intended to store logs before sending them to REST API
 - pipes.<pipe name>.source.work_dir - path to directory, where monitor log files
 - pipes.<pipe name>.source.file_name - name of log file
 - pipes.<pipe name>.source.rotated_file_name_pattern - log file rolling pattern
 - pipes.<pipe name>.source.charset_name - name of charset, used by Stream Client for sending logs
 - pipes.<pipe name>.source.record_separator - symbol, that separate each log record
 - pipes.<pipe name>.source.sleep_interval - interval to sleep, after read all log data
 - pipes.<pipe name>.source.failure_retry_limit - number of attempts to read logs, if occurred error while reading file data
 - pipes.<pipe name>.source.failure_sleep_interval - interval to sleep, if occurred error while reading file data
 - pipes.<pipe name>.sink.stream_name - name of target stream
 - pipes.<pipe name>.sink.host - server host
 - pipes.<pipe name>.sink.port - server port
 - pipes.<pipe name>.sink.ssl - Secure Socket Layer mode [true|false]
 - pipes.<pipe name>.sink.authToken - server security token
 - pipes.<pipe name>.sink.apiKey - ssl security key
 - pipes.<pipe name>.sink.writerPoolSize - number of threads, in which Stream Client sends events
 - pipes.<pipe name>.sink.version - reactor server version
 - pipes.<pipe name>.sink.packSize - number of logs sent at a time
 - pipes.<pipe name>.sink.failure_retry_limit - number of attempts to sent logs, if occurred error while reading file data
 - pipes.<pipe name>.sink.failure_sleep_interval - interval to sleep, if occurred error while sending logs
