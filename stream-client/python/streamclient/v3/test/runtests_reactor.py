#! /usr/bin/env python3
# -*- coding: utf-8 -*-

import unittest
import requests

from basicreactor import BasicReactor


class TestStreamClient(unittest.TestCase, BasicReactor):

    def setUp(self):
        self.config_file = 'config_reactor.json'
        self.base_set_up()

if '__main__' == __name__:
    unittest.main(warnings=False)
