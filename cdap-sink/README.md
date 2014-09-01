cdap-sink
==================

The Cdap sink is Flume Sink  implementation, which use RestStreamWriter to write events, recieved from source.

## Usage

 To use the Cdap sink, put Cdap Sink jar file to flume classpath (foe example to flume lib directory)
 Specify fully qualified name of your Cdap Sink class to Flume configuration properties
 
 a1.sinks.sink1.type = co.cask.cdap.flumesink.CdapSink
 
 input host name that is used by stream client:
   a1.sinks.sink1.host = <hostname or host ip>  
   
 input host port that is used by stream client:
   a1.sinks.sink1.port = <port>
 
 Also optional parameters can be specified
 
 Secure Socket Layer mode [true|false] (default false)
 a1.sinks.sink1.slEnabled = 
 
 Number of threads, in which Stream Client sends events:
 a1.sinks.sink1.writerPoolSize = 
 
 Server security token:
 a1.sinks.sink1.authToken =
 
 Ssl security key:
 a1.sinks.sink1.apiKey =
 eactor server version: ( default v2) 
 a1.sinks.sink1.version =
 Name of target stream:
 a1.sinks.sink1.streamName = 

## Example
   
Configuration of the flume agent, that reads data from log file and puts them to continuuity reactor using cdap sink.

a1.sources = r1
a1.channels = c1

a1.sources.r1.type = exec
a1.sources.r1.command = tail -F /tmp/log
a1.sources.r1.channels = c1

a1.sinks = k1
a1.sinks.k1.type = co.cask.cdap.flumesink.CdapSink

a1.sinks.k1.channel = c1
a1.sinks.k1.host  = 127.0.0.1
a1.sinks.k1.port = 10000
a1.sinks.k1.streamName = logEventStream
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100
