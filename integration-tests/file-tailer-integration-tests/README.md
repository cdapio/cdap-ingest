# Cask Data Application Platform (CDAP) File Tailer Integration Tests

This project contains integration tests for the CDAP File Tailer.

## Usage

Please review the general [CDAP ingest documentation](docs.cask.co/cdap/current) for usage instructions. This document 
contains information about project-specific configuration files.

## Configuration

 To configure integration tests against Standalone CDAP instance, edit:
 
 ```
 src/main/resources/local.conf 
 ```

 To configure integration tests against Standalone CDAP instance with authentication enabled, edit:
 
 ```
 src/main/resources/local_auth.conf 
 ```
 
 To configure integration tests against Distributed CDAP instance, edit:
 
 ```
 src/main/resources/remote.conf 
 ```

 To configure integration tests against Distributed CDAP instance with authentication enabled, edit:
 
 ```
 src/main/resources/remote_auth.conf 
 ```
 
 Please refer to the [CDAP File Tailer](docs.cask.co/cdap/current) for additional documentation.