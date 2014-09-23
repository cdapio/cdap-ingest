#! /usr/bin/env python3
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

import unittest
import httpretty
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


class TestStreamClient(unittest.TestCase):

    __dummy_host = 'dummy.host'
    __dummy_port = 65000
    __BASE_URL = 'http://{0}:{1}/v2'.format(__dummy_host, __dummy_port)
    __REQUEST_PLACEHOLDERS = {
        'streamid': '<streamid>'
    }
    __REQUESTS = {'streams': __BASE_URL + '/streams'}
    __REQUESTS['stream'] = '{0}/{1}'.format(__REQUESTS['streams'],
                                            __REQUEST_PLACEHOLDERS['streamid'])
    __REQUESTS['consumerid'] = '{0}/{1}'.format(__REQUESTS['stream'],
                                                'consumer-id')
    __REQUESTS['dequeue'] = '{0}/{1}'.format(__REQUESTS['stream'], 'dequeue')
    __REQUESTS['config'] = '{0}/{1}'.format(__REQUESTS['stream'], 'config')
    __REQUESTS['info'] = '{0}/{1}'.format(__REQUESTS['stream'], 'info')
    __REQUESTS['truncate'] = '{0}/{1}'.format(__REQUESTS['stream'], 'truncate')

    validStream = 'validStream'
    invalidStream = 'invalidStream'

    validFile = 'some.log'
    invalidFile = 'invalid.file'

    messageToWrite = 'some message'

    exit_code = 404

    def setUp(self):
        config = Config()
        config.host = self.__dummy_host
        config.port = self.__dummy_port

        self.sc = StreamClient(config)

    @httpretty.activate
    def test_create(self):
        url = self.__REQUESTS['stream'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
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
        url = self.__REQUESTS['stream'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
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
        url = self.__REQUESTS['config'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
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
        except NotFoundErrorn:
            self.fail('StreamClient.setTTL() failed')

    @httpretty.activate
    def test_set_ttl_invalid_stream(self):
        url = self.__REQUESTS['config'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
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
        url = self.__REQUESTS['info'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
            self.validStream
        )

        httpretty.register_uri(
            httpretty.GET,
            url,
            status=200,
            body='{"ttl": 88888}'
        )

        try:
            self.sc.get_ttl(self.validStream)
        except NotFoundError:
            self.fail('StreamClient.getTTL() failed')

    @httpretty.activate
    def test_get_ttl_invalid_stream(self):
        url = self.__REQUESTS['info'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
            self.invalidStream
        )

        httpretty.register_uri(
            httpretty.GET,
            url,
            status=404,
            body='{"ttl": 88888}'
        )

        self.assertRaises(
            NotFoundError,
            self.sc.get_ttl,
            self.invalidStream
        )

    @httpretty.activate
    def test_create_writer_successful(self):
        url = self.__REQUESTS['info'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
            self.validStream
        )

        httpretty.register_uri(
            httpretty.GET,
            url,
            status=200,
            body='{"ttl": 88888}'
        )

        self.assertIsInstance(
            self.sc.create_writer(self.validStream),
            StreamWriter
        )

    @httpretty.activate
    def test_create_writer_invalid_stream(self):
        url = self.__REQUESTS['info'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
            self.invalidStream
        )

        httpretty.register_uri(
            httpretty.GET,
            url,
            status=404,
            body='{"ttl": 88888}'
        )

        self.assertRaises(
            NotFoundError,
            self.sc.create_writer,
            self.invalidStream)

    @httpretty.activate
    def test_stream_writer_successful_writing(self):
        url = self.__REQUESTS['stream'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
            self.validStream
        )

        urlInfo = self.__REQUESTS['info'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
            self.validStream
        )

        httpretty.register_uri(
            httpretty.GET,
            urlInfo,
            status=200,
            body='{"ttl": 88888}'
        )

        httpretty.register_uri(
            httpretty.POST,
            url,
            status=200
        )

        sw = self.sc.create_writer(self.validStream)

        def onResponse(response):
            self.exit_code = response.status_code

        q = sw.write(self.messageToWrite)
        q.on_response(onResponse)

        self.assertEqual(self.exit_code, 200)

if '__main__' == __name__:
    unittest.main(warnings='ignore')
