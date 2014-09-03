#! /usr/bin/env python2
# -*- coding: utf-8 -*-

# Python 2.6 does not have 'unittest' module;
# try importing 'unittest2' instead.
try:
    import unittest2 as unittest
except ImportError:
    import unittest as unittest
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

    __dummy_host = u'dummy.host'
    __dummy_port = 65000
    __BASE_URL = u'http://{0}:{1}/v2'.format(__dummy_host, __dummy_port)
    __REQUEST_PLACEHOLDERS = {
        u'streamid': u'<streamid>'
    }
    __REQUESTS = {u'streams': __BASE_URL + u'/streams'}
    __REQUESTS[u'stream'] = u'{0}/{1}'.format(__REQUESTS[u'streams'],
                                            __REQUEST_PLACEHOLDERS[u'streamid'])
    __REQUESTS[u'consumerid'] = u'{0}/{1}'.format(__REQUESTS[u'stream'],
                                                u'consumer-id')
    __REQUESTS[u'dequeue'] = u'{0}/{1}'.format(__REQUESTS[u'stream'], u'dequeue')
    __REQUESTS[u'config'] = u'{0}/{1}'.format(__REQUESTS[u'stream'], u'config')
    __REQUESTS[u'info'] = u'{0}/{1}'.format(__REQUESTS[u'stream'], u'info')
    __REQUESTS[u'truncate'] = u'{0}/{1}'.format(__REQUESTS[u'stream'], u'truncate')

    validStream = u'validStream'
    invalidStream = u'invalidStream'

    validFile = u'some.log'
    invalidFile = u'invalid.file'

    messageToWrite = u'some message'

    exit_code = 404

    def setUp(self):
        config = Config()
        config.host = self.__dummy_host
        config.port = self.__dummy_port

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
            self.fail(u'StreamClient.setTTL() failed')

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
    def test_stream_writer_successful_sending(self):
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

        q = sw.send(self.validFile)
        q.on_response(on_response)

        self.assertEqual(self.exit_code, 200)

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

        q = sw.write(self.messageToWrite)
        q.on_response(on_response)

        self.assertEqual(self.exit_code, 200)

if u'__main__' == __name__:
    unittest.main()
