#! /usr/bin/env python2
# -*- coding: utf-8 -*-

# Python 2.6 does not have 'unittest' module;
# try importing 'unittest2' instead.
try:
    import unittest2 as unittest
except ImportError:
    import unittest as unittest
import requests

from basicreactor import BasicReactor


class TestStreamClient(unittest.TestCase, BasicReactor):

    def setUp(self):
        self.config_file = u'config_reactor.json'
        self.base_set_up()

if u'__main__' == __name__:
    unittest.main()
