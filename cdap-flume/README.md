CDAP FLUME
==================

The CDAP Sink is a Flume Sink implementation using RESTStreamWriter to write events received from a source.

## Usage

 To use, put the CDAP Sink jar file in the Flume classpath (for example, in the Flume lib directory).
 Specify the fully-qualified name of your CDAP Sink class in the Flume configuration properties:
 ```
 a1.sinks.sink1.type = co.cask.cdap.flume.StreamSink
 ```
 Enter the host name that is used by the stream client:
 ```
 a1.sinks.sink1.host = <hostname or host ip>  
 ```
 Target Stream name:
 ```
 a1.sinks.sink1.streamName = <Stream name>
 ```
 
 Optional parameters that can be specified are listed below with their default values.
  
 Enter the host port that is used by the stream client:
 ```
 a1.sinks.sink1.port = 10000
 ```
 Secure Socket Layer mode [true | false]
 ```
 a1.sinks.sink1.sslEnabled = false 
 ```
 Number of threads to which Stream Client can send events:
 ```
 a1.sinks.sink1.writerPoolSize = 10
 ```
 CDAP Gateway server version:
 ```
 a1.sinks.sink1.version = v2

 ```

## Authentication Client

 If you want to use authentication,add authentication client configuration parameters to sink configuration:
 
 Classpath of authentication client class:
 ````
 a1.sinks.sink1.authClientClass = co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient
 ````
 Path to authentication client properties file:
 ````
 a1.sinks.sink1.authClientProperties = /usr/local/apache-flume/conf/auth_client.conf
 ````

## Authentication Client Example Configuration
 
 ```
 
     # User name
     security.auth.client.username=admin
     # User password
     security.auth.client.password=realtime
     # Host name that is used by authentication client
     security.auth.client.gateway.hostname=localhost
     # Host port name that is used by authentication client
     security.auth.client.gateway.port=10000
     # SSL mode [true|false]
     security.auth.client.gateway.ssl.enabled=false
 
## Example
   
 Configuration of the Flume agent that reads data from a log file and puts them to CDAP using CDAP Sink.
 ```
 a1.sources = r1
 a1.channels = c1
 a1.sources.r1.type = exec
 a1.sources.r1.command = tail -F /tmp/log
 a1.sources.r1.channels = c1
 a1.sinks = k1
 a1.sinks.k1.type = co.cask.cdap.flume.StreamSink
 a1.sinks.k1.channel = c1
 a1.sinks.k1.host  = 127.0.0.1
 a1.sinks.k1.port = 10000
 a1.sinks.k1.streamName = logEventStream
 a1.channels.c1.type = memory
 a1.channels.c1.capacity = 1000
 a1.channels.c1.transactionCapacity = 100
 ```