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
```> python stream_integration_test.py```
```> python stream_integration_test_ssl.py```

## To configure tests change these files:
```cdap_config.json```             - for integration with Standalone CDAP
```cdap_ssl_config.json```         - for integration with SSL and Auth enabled CDAP

## More server instances to tests.
New tests should be derived from *StreamTestBase* and *unittest.TestCase* classes.
The child object should initialize *self.config_file* variable in *setUp* method
and call *base_set_up* of *StreamTestBase* to initialize tests.

Example:

```
class TestStreamClient(unittest.TestCase, StreamTestBase):

    def setUp(self):
        self.config_file = 'cdap_config.json'
        self.base_set_up()
```

## License and Trademarks

Copyright © 2014 Cask Data, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
