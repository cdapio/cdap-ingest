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
import os
import sys
import inspect
import threading
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

    valid_stream = u'validStream'
    invalid_stream = u'invalidStream'

    valid_file = u'some.log'
    invalid_file = u'invalid.file'

    message_to_write = u'some_message'
    event_number = 50

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
        self.event_latch = EventLatch(self.event_number)

    def test_reactor_successful_connection(self):

        # Create stream
        self.sc.create(self.valid_stream)
        get_stream_url = '/v2/streams/%s' % self.valid_stream
        res_stream = self.get_data_from_cdap(get_stream_url)
        self.assertEqual(res_stream["name"], self.valid_stream)

        # Set ttl
        ttl = 88888
        self.sc.set_ttl(self.valid_stream, ttl)
        self.assertEqual(self.sc.get_ttl(self.valid_stream), ttl)

        #Get ttl and verify
        self.sc.get_ttl(self.valid_stream)

        # Succesfully create a writer for the stream
        self.assertIsInstance(
            self.sc.create_writer(self.valid_stream),
            StreamWriter
        )

        # Write to this stream
        sw = self.sc.create_writer(self.valid_stream)

        def on_response(response):
            self.exit_code = response.status_code
            self.event_latch.count_down()

        def check_exit_code(response):
            self.assertEqual(self.exit_code, 200)

        start_time = int(round(time.time() * 1000))
        event_bodies = [self.message_to_write + str(i) for i in xrange(50)]

        for event in event_bodies:
            q = sw.write(event)
            q.on_response(on_response)
            q.on_response(check_exit_code, check_exit_code)
        # time.sleep(1)
        self.event_latch.wait_for_complite()
        end_time = int(round(time.time() * 1000))
        event_request_url = \
            u'/v2/streams/%s/events?start=%s&end=%s' % (self.valid_stream,
                                                        start_time, end_time)
        received_events = self.get_data_from_cdap(event_request_url)
        self.assertEqual(self.event_number, len(received_events))
        expected_events = [self.message_to_write + str(i) for i in xrange(50)]
        received_event_bodies = [event['body'] for event in received_events]
        self.assertTrue(set(expected_events) == set(received_event_bodies))

        # Truncate from the stream
        self.sc.truncate(self.valid_stream)
        event_request_url = u'/v2/streams/%s/events' % self.valid_stream
        received_events = self.get_data_from_cdap(event_request_url)
        self.assertIsNone(received_events)

    def test_set_ttl_invalid_stream(self):
        ttl = 88888

        self.assertRaises(
            NotFoundError,
            self.sc.set_ttl,
            self.invalid_stream,
            ttl
        )

        self.assertRaises(
            NotFoundError,
            self.sc.get_ttl,
            self.invalid_stream
        )

        self.assertRaises(
            NotFoundError,
            self.sc.create_writer,
            self.invalid_stream)

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


class EventLatch(object):
    def __init__(self, events_count):
        self.events_count = events_count
        self.lock = threading.Condition()

    def count_down(self):
        self.lock.acquire()
        self.events_count -= 1
        if self.events_count <= 0:
            self.lock.notifyAll()
        self.lock.release()

    def wait_for_complite(self):
        self.lock.acquire()
        while self.events_count > 0:
            self.lock.wait()
        self.lock.release()