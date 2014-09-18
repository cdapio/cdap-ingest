#! /usr/bin/env python3
# -*- coding: utf-8 -*-

import unittest
import requests

from basicreactor import BasicReactor


class TestStreamClient(unittest.TestCase, BasicReactor):

    def setUp(self):
        self.host = '166.78.96.4'
        self.port = 10000
        self.ssl = False
        self.ssl_cert_check = False
        self.set_up()

if u'__main__' == __name__:
    unittest.main()
