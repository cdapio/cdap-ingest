# -*- coding: utf-8 -*-

#  Copyright © 2014 Cask Data, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.


import inspect
import json
import os
import sys
currentdir = os.path.dirname(
    os.path.abspath(inspect.getfile(inspect.currentframe())))
parentdir = os.path.dirname(currentdir)
sys.path.insert(0, parentdir)

from cdap_stream_client import Config, StreamWriter, StreamClient
from cdap_stream_client.serviceconnector import NotFoundError
from cdap_auth_client import BasicAuthenticationClient

# Should be used as parent class for integration tests.
# In children 'config_file' property has to be set and
# 'base_set_up' method called.


class StreamTestBase(object):


    validStream = u'validStream'
    invalidStream = u'invalidStream'

    validFile = u'some.log'
    invalidFile = u'invalid.file'

    messageToWrite = u'some message'

    exit_code = 404

    @property
    def config_file(self):
        return self.__config_file

    @config_file.setter
    def config_file(self, filename):
        self.__config_file = filename


    @staticmethod
    def read_from_file(filename):
        with open(filename) as configFile:
            json_config = json.loads(configFile.read())

        new_config = Config()
        new_config.host = json_config.get(u'hostname', new_config.host)
        new_config.port = json_config.get(u'port', new_config.port)
        new_config.ssl = json_config.get(u'SSL', new_config.ssl)
        new_config.ssl_cert_check = json_config.get(u'security_ssl_cert_check', new_config.ssl_cert_check)
        return new_config

    def base_set_up(self):
        with open(self.config_file) as config_file:
            auth_config = json.loads(config_file.read())
        self.config = StreamTestBase.read_from_file(self.config_file)

        auth_client = BasicAuthenticationClient()
        auth_client.set_connection_info(self.config.host,
                                       self.config.port, self.config.ssl)
        if auth_client.is_auth_enabled():
            auth_client.configure(auth_config)
            self.config.set_auth_client(auth_client)

        self.sc = StreamClient(self.config)

    def test_reactor_successful_connection(self):
        # Create stream
        self.sc.create(self.validStream)

        # Set ttl
        ttl = 88888
        self.sc.set_ttl(self.validStream, ttl)

        #Get ttl and verify
        self.sc.get_ttl(self.validStream)

        # Succesfully create a writer for the stream
        self.assertIsInstance(
            self.sc.create_writer(self.validStream),
            StreamWriter
        )

        # Write to this stream
        sw = self.sc.create_writer(self.validStream)

        def on_response(response):
            self.exit_code = response.status_code

        def check_exit_code(response):
            self.assertEqual(self.exit_code, 200)

        q = sw.write(self.messageToWrite)
        q.on_response(on_response)
        q.on_response(check_exit_code, check_exit_code)

    def test_set_ttl_invalid_stream(self):
        ttl = 88888

        self.assertRaises(
            NotFoundError,
            self.sc.set_ttl,
            self.invalidStream,
            ttl
        )

        self.assertRaises(
            NotFoundError,
            self.sc.get_ttl,
            self.invalidStream
        )

        self.assertRaises(
            NotFoundError,
            self.sc.create_writer,
            self.invalidStream)
