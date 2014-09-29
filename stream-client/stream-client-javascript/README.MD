# CDAP Client JavaScript library

Client JavaScript API for sending request to CASK reactor.

## Supported Actions

- sending request to gateway server

## Usage

 To use the Client JavaScript API, include these scrip tag in your index.html:

In web browser:
```
<script type="text/javascript" src="cask-client.min.js"></script>
```

In Node.JS:
```
var CDAPTracker = require('serviceconnector');
```

## Tracker object
Methods:

  'track(url, data)' - makes POST request including *data* to *url*. *data* should be an object.
                       Returns a promise object.

## Promise object:

  'then(successCallback, errorCallback, notifyCallback)     - regardless of when the promise was or will be resolved or rejected, then calls one of the success or error callbacks asynchronously as soon as the result is available. The callbacks are called with a single argument: the result or rejection reason. Additionally, the notify callback may be called zero or more times to provide a progress indication, before the promise is resolved or rejected.

  'catch(errorCallback)'        - shorthand for 'then(null, errorCallback)'
  'finally(callback)'           – allows to observe either the fulfillment or rejection of a promise, but to do so without modifying the final value.

## Example

Create a ```ServiceConnector``` instance, specifying the fields 'host' and 'port' of the gateway server. 
Optional configurations that can be set (and their default values):

  - ssl: False (use HTTP protocol)

```
    var tracker = new CDAPTracker.ServiceConnector('server_name', 10000),
        pr = tracker.track('/path/to/request', {});
```
