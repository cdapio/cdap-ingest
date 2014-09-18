# HOWTO Run tests

Tests are located in ```test``` directory of the source tree root.

## Unittest
In command shell type this:
```> python runtests.py```

## Integration tests
### Change server connection information in ```runtests_reactor*.py```:

```
self.host = '166.78.96.4'                        - server name or IP address
self.port = 10000                                - port service is working on
self.ssl = False                                 - SSL on/off
self.ssl_cert_check = False                      - ```True``` - ignores self-signed certificates.
                                                   ```False``` - checks if certificates are correctly signed.
```

In command shell type this:
```> python runtests_reactor.py```
```> python runtests_reactor_ssl.py```