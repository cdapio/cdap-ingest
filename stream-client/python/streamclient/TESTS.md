# HOWTO Run tests

Tests are located in ```test``` directory of the source tree root.

## Unittest
In command shell type this:
```> python runtests.py```

## Integration tests

```
self.config_file = 'config_name.json'
```

In command shell type this:
```> python runtests_reactor.py```
```> python runtests_reactor_ssl.py```

## To configure tests change these files:
```config.json```                  - for unit tests.
```config_reactor.json```          - for integration with Singlenode reactor
```config_reactor.json```          - for integration with SSL and Auth enabled reactor

## More server instances to tests.
New tests should be derived from *BasicReactor* and *unittest.TestCase* classes.
The child object should initialize *self.config_file* variable in *setUp* method
and call *base_set_up* of *BasicReactor* to initialize tests.

Example:

```
class TestStreamClient(unittest.TestCase, BasicReactor):

    def setUp(self):
        self.config_file = 'config_reactor.json'
        self.base_set_up()
```
