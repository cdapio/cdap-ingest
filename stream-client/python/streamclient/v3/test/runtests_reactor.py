#! /usr/bin/env python3
# -*- coding: utf-8 -*-

import unittest
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
from caskauthclient.BasicAuthenticationClient import BasicAuthenticationClient


class TestStreamClient(unittest.TestCase):

    __host = 'localhost'
    __port = 10000
    __ssl = False
    __BASE_URL = 'http://{0}:{1}/v2'.format(__host, __port)
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
        authClient = BasicAuthenticationClient()
        authClient.set_connection_info(self.__host, self.__port, self.__ssl)
        authClient.configure('config.json')

        config = Config()
        config.host = self.__host
        config.port = self.__port
        config.ssl = self.__ssl
        config.set_auth_client(authClient)

        self.sc = StreamClient(config)

    def test_reactor_successful_connection(self):
        try:
            self.sc.create(self.validStream)
        except:
            self.fail('Reactor connection failed')

    def test_reactor_failure_connection(self):
        url = self.__REQUESTS['stream'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
            self.validStream
        )

        url = url.replace('{0}'.format(self.__port), '60000')

        self.assertRaises(
            Exception,
            requests.put,
            url
            )

    def test_create(self):
        url = self.__REQUESTS['stream'].replace(
            self.__REQUEST_PLACEHOLDERS['streamid'],
            self.validStream
        )

        response = requests.put(url)

        self.assertEqual(response.status_code, 200)

    def test_set_ttl_valid_stream(self):
        ttl = 88888

        try:
            self.sc.set_ttl(self.validStream, ttl)
        except NotFoundError:
            self.fail('StreamClient.setTTL() failed')

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
            self.fail('StreamClient.getTTL() failed')

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

        q = sw.send(self.validFile)
        q.on_response(on_response)

        self.assertEqual(self.exit_code, 200)

    def test_stream_writer_successful_writing(self):
        sw = self.sc.create_writer(self.validStream)

        def on_response(response):
            self.exit_code = response.status_code

        q = sw.write(self.messageToWrite)
        q.on_response(on_response)

        self.assertEqual(self.exit_code, 200)

if '__main__' == __name__:
    unittest.main()
