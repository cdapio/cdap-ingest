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

import requests

import os
import sys
import inspect
currentdir = os.path.dirname(
    os.path.abspath(inspect.getfile(inspect.currentframe())))
parentdir = os.path.dirname(currentdir)
sys.path.insert(0, parentdir)

from config import Config
from serviceconnector import NotFoundError
from streamwriter import StreamWriter
from streamclient import StreamClient
from cdap_auth_client.BasicAuthenticationClient import BasicAuthenticationClient
from cdap_auth_client.Config import Config as AuthConfig

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

    def base_set_up(self):
        authConfig = AuthConfig().read_from_file(self.config_file)
        self.config = Config.read_from_file(self.config_file)

        authClient = BasicAuthenticationClient()
        authClient.set_connection_info(self.config.host,
                                       self.config.port, self.config.ssl)
        authClient.configure(authConfig)

        self.config.set_auth_client(authClient)

        self.sc = StreamClient(self.config)

        self.__BASE_URL = u'http://{0}:{1}/v2'.format(
            self.config.host, self.config.port)
        self.__REQUEST_PLACEHOLDERS = {
            u'streamid': u'<streamid>'
        }
        self.__REQUESTS = {u'base_stream_path': self.__BASE_URL + u'/streams'}
        self.__REQUESTS[u'stream'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'base_stream_path'],
            self.__REQUEST_PLACEHOLDERS[u'streamid'])
        self.__REQUESTS[u'consumerid'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'consumer-id')
        self.__REQUESTS[u'dequeue'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'dequeue')
        self.__REQUESTS[u'config'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'config')
        self.__REQUESTS[u'info'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'info')
        self.__REQUESTS[u'truncate'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'truncate')

    def test_reactor_successful_connection(self):
        try:
            self.sc.create(self.validStream)
        except:
            self.fail(u'Reactor connection failed')

    def test_reactor_failure_connection(self):
        url = self.__REQUESTS[u'stream'].replace(
            self.__REQUEST_PLACEHOLDERS[u'streamid'],
            self.validStream
        )

        url = url.replace(u'{0}'.format(self.config.port), u'0')

        self.assertRaises(
            Exception,
            requests.get,
            url
            )

    def test_create(self):
        url = self.__REQUESTS[u'stream'].replace(
            self.__REQUEST_PLACEHOLDERS[u'streamid'],
            self.validStream
        )

        response = requests.put(url)

        self.assertEqual(response.status_code, 200)

    def test_set_ttl_valid_stream(self):
        ttl = 88888

        try:
            self.sc.set_ttl(self.validStream, ttl)
        except NotFoundError:
            self.fail(u'StreamClient.set_ttl() failed')

    def test_set_ttl_invalid_stream(self):
        ttl = 88888

        self.assertRaises(
            NotFoundError,
            self.sc.set_ttl,
            self.invalidStream,
            ttl
        )

    def test_get_ttl_valid_stream(self):
        try:
            self.sc.get_ttl(self.validStream)
        except NotFoundError:
            self.fail(u'StreamClient.getTTL() failed')

    def test_get_ttl_invalid_stream(self):
        self.assertRaises(
            NotFoundError,
            self.sc.get_ttl,
            self.invalidStream
        )

    def test_create_writer_successful(self):
        self.assertIsInstance(
            self.sc.create_writer(self.validStream),
            StreamWriter
        )

    def test_create_writer_invalid_stream(self):

        self.assertRaises(
            NotFoundError,
            self.sc.create_writer,
            self.invalidStream)

    def test_stream_writer_successful_sending(self):
        sw = self.sc.create_writer(self.validStream)

        def on_response(response):
            self.exit_code = response.status_code

        def check_exit_code(response):
            self.assertEqual(self.exit_code, 200)

        q = sw.send(self.validFile)
        q.on_response(on_response)
        q.on_response(check_exit_code, check_exit_code)

    def test_stream_writer_successful_writing(self):
        sw = self.sc.create_writer(self.validStream)

        def on_response(response):
            self.exit_code = response.status_code

        def check_exit_code(response):
            self.assertEqual(self.exit_code, 200)

        q = sw.write(self.messageToWrite)
        q.on_response(on_response)
        q.on_response(check_exit_code, check_exit_code)
