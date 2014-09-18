#! /usr/bin/env python3
# -*- coding: utf-8 -*-

import unittest
import requests

from basicreactor import BasicReactor


class TestStreamClient(unittest.TestCase, BasicReactor):

    def setUp(self):
        self.host = '198.61.160.4'
        self.port = 10443
        self.ssl = True
        self.ssl_cert_check = False
        self.set_up()

if u'__main__' == __name__:
    unittest.main()
