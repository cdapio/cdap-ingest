#! /usr/bin/env python3
# -*- coding: utf-8 -*-

import unittest
from serviceconnector import NoFoundException
from streamwriter import StreamWriter
from streamclient import *

class TestStreamClient(unittest.TestCase):

    def setUp(self):
        self.validStream = 'validStream'
        self.invalidStream = 'invalidStream'

        self.validFile = 'some.log'
        self.invalidFile = 'invalid.file'

        self.messageToWrite = 'some message'

        self.sc = StreamClient()
        self.sc.create(self.validStream)

    def test_set_ttl_valid_stream(self):
        ttl = 86400
        self.sc.setTTL(self.validStream, ttl)

        responseTTL = self.sc.getTTL(self.validStream)
        self.assertEqual(responseTTL, ttl)

    def test_set_ttl_invalid_stream(self):
        ttl = 86400

        self.assertRaises(
            NoFoundException,
            self.sc.setTTL,
            self.invalidStream,
            ttl
        )

    def test_get_ttl_invalid_stream(self):
        self.assertRaises(
            NoFoundException,
            self.sc.getTTL,
            self.invalidStream
        )

    def test_get_ttl_valid_stream(self):
        self.assertIsNotNone(
            self.sc.getTTL(self.validStream)
        )

    def test_create_writer(self):
        self.assertIsInstance(
            self.sc.createWriter(self.validStream),
            StreamWriter
        )

    def test_stream_writer_successful_sending(self):
        sw = self.sc.createWriter(self.validStream)

        def onResponse(response):
            self.exit_code = response.status

        q = sw.send(self.validFile)
        q.onResponse(onResponse)

        self.assertEqual(self.exit_code, 200)

    def test_stream_writer_invalid_stream_sending(self):
        sw = self.sc.createWriter(self.invalidStream)

        def onResponse(response):
            self.exit_code = response.status

        q = sw.send(self.validFile)
        q.onResponse(onResponse)

        self.assertNotEqual(self.exit_code, 200)

    def test_stream_writer_successful_writing(self):
        sw = self.sc.createWriter(self.validStream)

        def onResponse(response):
            self.exit_code = response.status

        q = sw.write(self.messageToWrite)
        q.onResponse(onResponse)

        self.assertEqual(self.exit_code, 200)

    def test_stream_writer_invalid_stream_writing(self):
        sw = self.sc.createWriter(self.invalidStream)

        def onResponse(response):
            self.exit_code = response.status

        q = sw.write(self.messageToWrite)
        q.onResponse(onResponse)

        self.assertNotEqual(self.exit_code, 200)

if '__main__' == __name__:
    unittest.main()
