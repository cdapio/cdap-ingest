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
import json


import json
import os
import sys
import inspect
import time
import httplib
import requests


currentdir = os.path.dirname(
    os.path.abspath(inspect.getfile(inspect.currentframe())))
parentdir = os.path.dirname(currentdir)
sys.path.insert(0, parentdir)

from cdap_stream_client.config import Config
from cdap_stream_client.serviceconnector import NotFoundError
from cdap_stream_client.streamwriter import StreamWriter
from cdap_stream_client.streamclient import StreamClient
from cdap_auth_client.BasicAuthenticationClient \
    import BasicAuthenticationClient

# Should be used as parent class for integration tests.
# In children 'config_file' property has to be set and
# 'base_set_up' method called.


class StreamTestBase(object):

    validStream = u'validStream'
    invalidStream = u'invalidStream'

    validFile = u'some.log'
    invalidFile = u'invalid.file'

    messageToWrite = u'some_message'
    eventsNumber = 50

    exit_code = 404

    @property
    def config_file(self):
        return self.__config_file

    @config_file.setter
    def config_file(self, filename):
        self.__config_file = filename

    @property
    def auth_config_file(self):
        return self.__auth_config_file

    @auth_config_file.setter
    def auth_config_file(self, filename):
        self.__auth_config_file = filename

    @staticmethod
    def read_from_file(filename):
        with open(filename) as configFile:
            json_config = json.loads(configFile.read())

        new_config = Config()
        new_config.host = json_config.get(u'hostname',
                                          new_config.host)
        new_config.port = json_config.get(u'port', new_config.port)
        new_config.ssl = json_config.get(u'SSL', new_config.ssl)
        new_config.ssl_cert_check = \
            json_config.get(u'security_ssl_cert_check',
                            new_config.ssl_cert_check)
        return new_config

    def base_set_up(self):
        with open(self.auth_config_file) as auth_conf_file:
            auth_config = json.loads(auth_conf_file.read())
        self.config = StreamTestBase.read_from_file(self.config_file)
        self.auth_client = BasicAuthenticationClient()
        self.auth_client.set_connection_info(self.config.host,
                                             self.config.port,
                                             self.config.ssl)
        if self.auth_client.is_auth_enabled():
            self.auth_client.configure(auth_config)
            self.config.set_auth_client(self.auth_client)
        self.sc = StreamClient(self.config)

    def test_reactor_successful_connection(self):

        # Create stream
        self.sc.create(self.validStream)
        get_stream_url = '/v2/streams/%s' % self.validStream
        res_stream = self.get_data_from_cdap(get_stream_url)
        self.assertEqual(res_stream["name"], self.validStream)

        # Set ttl
        ttl = 88888
        self.sc.set_ttl(self.validStream, ttl)
        self.assertEqual(self.sc.get_ttl(self.validStream), ttl)

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

        start_time = int(round(time.time() * 1000))

        for i in xrange(50):
            q = sw.write(self.messageToWrite + str(i))
            q.on_response(on_response)
            q.on_response(check_exit_code, check_exit_code)
        time.sleep(1)
        end_time = int(round(time.time() * 1000))
        event_request_url = \
            u'/v2/streams/%s/events?start=%s&end=%s' % (self.validStream,
                                                        start_time, end_time)
        received_events = self.get_data_from_cdap(event_request_url)
        for i in xrange(50):
            self.assertTrue(any(event['body'] ==
                                self.messageToWrite +
                                str(i) for event in received_events))

        # Truncate from the stream
        self.sc.truncate(self.validStream)
        event_request_url = u'/v2/streams/%s/events' % self.validStream
        received_events = self.get_data_from_cdap(event_request_url)
        self.assertIsNone(received_events)

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

    def get_data_from_cdap(self, request_url):
        base_url = u'%s://%s:%d' % ("https" if self.config.ssl else "http",
                                    self.config.host, self.config.port)
        url = base_url + request_url
        if self.auth_client.is_auth_enabled():
            token = self.auth_client.get_access_token()
            headers = {'Authorization': token.token_type + " " + token.value}
            response = requests.get(url, headers=headers)
        else:
            response = requests.get(url)
        if response.status_code == httplib.NO_CONTENT:
            return None
        else:
            return response.json()
