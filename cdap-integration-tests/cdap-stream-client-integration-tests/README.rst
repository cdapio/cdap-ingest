.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2014-2015 Cask Data, Inc.
    :license: See LICENSE file in this repository

=====================================================================
Cask Data Application Platform (CDAP) Stream Client Integration Tests
=====================================================================

This project contains integration tests for the CDAP Stream Client Java API.

Usage
=====

Please review the general `CDAP ingest documentation <http://docs.cask.co/cdap/current/>`__
for usage instructions. This document contains information about project-specific
configuration files.

Configuration
=============

To configure integration tests against Standalone CDAP instance, edit::

  src/main/resources/local.conf 


To configure integration tests against Standalone CDAP instance with authentication
enabled, edit::

  src/main/resources/local_auth.conf


To configure integration tests against Distributed CDAP instance, edit::

  src/main/resources/remote.conf


To configure integration tests against Distributed CDAP instance with authentication
enabled, edit::

  src/main/resources/remote_auth.conf 


Please refer to *CDAP Java Stream Client* in the `CDAP documentation
<http://docs.cask.co/cdap/current/>`__ for additional information.
