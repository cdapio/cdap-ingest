#! /usr/bin/env python2
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

# Python 2.6 does not have 'unittest' module;
# try importing 'unittest2' instead.
try:
    import unittest2 as unittest
except ImportError:
    import unittest as unittest
import mock

import httpretty
import requests

import os
import sys
import inspect
current_dir = os.path.dirname(
    os.path.abspath(inspect.getfile(inspect.currentframe())))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)

from cdap_stream_client import Config, StreamWriter, StreamClient
from cdap_stream_client.serviceconnector import NotFoundError

with mock.patch('__main__.Config.is_auth_enabled',
                        new_callable=mock.PropertyMock) \
    as mock_auth_enabled:

    mock_auth_enabled.return_value = False


    class TestStreamClient(unittest.TestCase):

        # Should be the same as 'hostname' and 'port' fields in 'default-config.json'
        # file to make tests work right.
        __dummy_host = u'dummy.host'
        __dummy_port = 65000
        __BASE_URL = u'http://{0}:{1}/v2'.format(__dummy_host, __dummy_port)
        __REQUEST_PLACEHOLDERS = {
            u'streamid': u'<streamid>'
        }
        __REQUESTS = {u'base_stream_path': __BASE_URL + u'/streams'}
        __REQUESTS[u'stream'] = u'{0}/{1}'.format(
            __REQUESTS[u'base_stream_path'],
            __REQUEST_PLACEHOLDERS[u'streamid'])
        __REQUESTS[u'consumerid'] = u'{0}/{1}'.format(__REQUESTS[u'stream'],
                                                      u'consumer-id')
        __REQUESTS[u'dequeue'] = u'{0}/{1}'.format(__REQUESTS[u'stream'],
                                                   u'dequeue')
        __REQUESTS[u'config'] = u'{0}/{1}'.format(__REQUESTS[u'stream'],
                                                  u'config')
        __REQUESTS[u'info'] = u'{0}/{1}'.format(__REQUESTS[u'stream'], u'info')
        __REQUESTS[u'truncate'] = u'{0}/{1}'.format(__REQUESTS[u'stream'],
                                                    u'truncate')

        validStream = u'validStream'
        invalidStream = u'invalidStream'

        validFile = u'some.log'
        invalidFile = u'invalid.file'

        messageToWrite = u'some message'

        exit_code = 404

        def setUp(self):
            config = Config.read_from_file(os.path.join(os.path.dirname(__file__), u"default-config.json"))

            self.sc = StreamClient(config)

        @httpretty.activate
        def test_create(self):
            url = self.__REQUESTS[u'stream'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.validStream
            )

            httpretty.register_uri(
                httpretty.PUT,
                url,
                status=200
            )

            response = requests.put(url)

            self.assertEqual(response.status_code, 200)

        @httpretty.activate
        def test_create_fail(self):
            url = self.__REQUESTS[u'stream'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.validStream
            )

            httpretty.register_uri(
                httpretty.PUT,
                url,
                status=404
            )

            response = requests.put(url)

            self.assertNotEqual(response.status_code, 200)

        @httpretty.activate
        def test_set_ttl_valid_stream(self):
            url = self.__REQUESTS[u'config'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.validStream
            )
            ttl = 88888

            httpretty.register_uri(
                httpretty.PUT,
                url,
                status=200
            )

            try:
                self.sc.set_ttl(self.validStream, ttl)
            except NotFoundError:
                self.fail(u'StreamClient.set_ttl() failed')

        @httpretty.activate
        def test_set_ttl_invalid_stream(self):
            url = self.__REQUESTS[u'config'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.invalidStream
            )
            ttl = 88888

            httpretty.register_uri(
                httpretty.PUT,
                url,
                status=404
            )

            self.assertRaises(
                NotFoundError,
                self.sc.set_ttl,
                self.invalidStream,
                ttl
            )

        @httpretty.activate
        def test_get_ttl_valid_stream(self):
            url = self.__REQUESTS[u'info'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.validStream
            )

            httpretty.register_uri(
                httpretty.GET,
                url,
                status=200,
                body=u'{"ttl": 88888}'
            )

            try:
                self.sc.get_ttl(self.validStream)
            except NotFoundError:
                self.fail(u'StreamClient.getTTL() failed')

        @httpretty.activate
        def test_get_ttl_invalid_stream(self):
            url = self.__REQUESTS[u'info'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.invalidStream
            )

            httpretty.register_uri(
                httpretty.GET,
                url,
                status=404,
                body=u'{"ttl": 88888}'
            )

            self.assertRaises(
                NotFoundError,
                self.sc.get_ttl,
                self.invalidStream
            )

        @httpretty.activate
        def test_create_writer_successful(self):
            url = self.__REQUESTS[u'info'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.validStream
            )

            httpretty.register_uri(
                httpretty.GET,
                url,
                status=200,
                body=u'{"ttl": 88888}'
            )

            self.assertIsInstance(
                self.sc.create_writer(self.validStream),
                StreamWriter
            )

        @httpretty.activate
        def test_create_writer_invalid_stream(self):
            url = self.__REQUESTS[u'info'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.invalidStream
            )

            httpretty.register_uri(
                httpretty.GET,
                url,
                status=404,
                body=u'{"ttl": 88888}'
            )

            self.assertRaises(
                NotFoundError,
                self.sc.create_writer,
                self.invalidStream)

        @httpretty.activate
        def test_stream_writer_successful_writing(self):
            url = self.__REQUESTS[u'stream'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.validStream
            )

            urlInfo = self.__REQUESTS[u'info'].replace(
                self.__REQUEST_PLACEHOLDERS[u'streamid'],
                self.validStream
            )

            httpretty.register_uri(
                httpretty.GET,
                urlInfo,
                status=200,
                body=u'{"ttl": 88888}'
            )

            httpretty.register_uri(
                httpretty.POST,
                url,
                status=200
            )

            sw = self.sc.create_writer(self.validStream)

            def on_response(response):
                self.exit_code = response.status_code

            def check_exit_code(response):
                self.assertEqual(self.exit_code, 200)

            q = sw.write(self.messageToWrite)
            q.on_response(on_response)
            q.on_response(check_exit_code)

    if u'__main__' == __name__:
        unittest.main()
