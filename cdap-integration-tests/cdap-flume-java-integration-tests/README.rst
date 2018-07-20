.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2014-2017 Cask Data, Inc.
    :license: See LICENSE file in this repository

==================================================================
Cask Data Application Platform (CDAP) Flume Sink Integration Tests
==================================================================

This project contains integration tests for CDAP Flume Sink.

Usage
=====

Please review the general `CDAP ingest documentation <http://docs.cask.co/cdap/current/>`__
for usage instructions. This document contains information about project-specific
configuration files.

Configuration
=============

To configure integration tests against a CDAP Local Sandbox instance, edit::

  src/test/resources/local.conf


To configure integration tests against a CDAP Local Sandbox instance with authentication
enabled, edit::

  src/test/resources/local_auth.conf


To configure integration tests against a CDAP Local Sandbox instance with authentication
enabled and ssl turned on, edit::

  src/test/resources/local_auth_ssl.conf


To configure integration tests against a Distributed CDAP instance, edit::

  src/test/resources/remote.conf


To configure integration tests against a Distributed CDAP instance with authentication
enabled, edit::

  src/test/resources/remote_auth.conf


To configure integration tests against a Distributed CDAP instance with authentication
enabled and ssl turned on, edit::

  src/test/resources/remote_auth_ssl.conf

Please refer to *CDAP Flume Sink* in the `CDAP documentation
<http://docs.cask.co/cdap/current/>`__ for additional information.
