.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright © 2015 Cask Data, Inc.
    :license: See LICENSE file in this repository

===================================================================
Cask Data Application Platform (CDAP) File Tailer Integration Tests
===================================================================

This project contains integration tests for the CDAP File Tailer.

*Note*: The File Tailer is no longer supported for CDAP 3.0. Most likely, it will still
work, but because File Tailer is not aware of CDAP Namespaces, it would only work with
the default namespace.

Usage
=====

Please review the general `CDAP ingest documentation
<http://docs.cask.co/cdap/current/>`__ for usage instructions. This document contains
information about project-specific configuration files.

Configuration
=============

To configure integration tests against a Standalone CDAP instance, edit::

  src/main/resources/local.conf 


To configure integration tests against a Standalone CDAP instance with authentication
enabled, edit::

  src/main/resources/local_auth.conf 


To configure integration tests against a Standalone CDAP instance with authentication
enabled and ssl turned on, edit::

  src/main/resources/local_auth_ssl.conf 


To configure integration tests against a Distributed CDAP instance, edit::

  src/main/resources/remote.conf 


To configure integration tests against a Distributed CDAP instance with authentication
enabled, edit::

  src/main/resources/remote_auth.conf 


To configure integration tests against a Distributed CDAP instance with authentication
enabled and ssl turned on, edit::

  src/main/resources/remote_auth_ssl.conf 


Please refer to *CDAP File Tailer* in the `CDAP documentation
<http://docs.cask.co/cdap/current/>`__ for additional information.
