# Cask Data Application Platform (CDAP) Flume Sink Integration Tests

This project contains integration tests for CDAP Flume Sink.

## Usage

Please review the general [CDAP ingest documentation](docs.cask.co/cdap/current) for usage instructions. This document
contains information about project-specific configuration files.

## Configuration

 To configure integration tests against Standalone CDAP instance, edit:
 
 ```
 src/test/resources/local.conf 
 ```

 To configure integration tests against Standalone CDAP instance with authentication enabled, edit:
 
 ```
 src/test/resources/local_auth.conf 
 ```
 
 To configure integration tests against Distributed CDAP instance, edit:
 
 ```
 src/test/resources/remote.conf 
 ```

 To configure integration tests against Distributed CDAP instance with authentication enabled, edit:
 
 ```
 src/test/resources/remote_auth.conf 
 ```
