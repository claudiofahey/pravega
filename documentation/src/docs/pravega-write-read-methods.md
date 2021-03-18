---
title: Methods for Writing and Reading Pravega Streams
---

import { IfHaveFeature, IfMissingFeature, SDPVersion } from 'nautilus-docs';

This document describes the methods that can be used to write or read data in Pravega streams.

## Overview

Method                                                                           | Write | Transactional Write | Read | Special Features | Notes
---------------------------------------------------------------------------------|-------|---------------------|------|------------------|------------------------------------------------------
[Pravega Sensor Collector](#pravega-sensor-collector)                            | Yes   | Yes                 | No   | EO               |
SDP Ingest Gateway                                                               | Yes   | No                  | No   |                  |
[Pravega Search](#pravega-search)                                                | Yes   | Yes                 | Yes  | EO               |
Apache Flink                                                                     | Yes   | Yes                 | Yes  | EO               |
Apache Spark                                                                     | Yes   | Yes                 | Yes  | EO               |
[Apache NiFi](https://github.com/pravega/nifi-pravega)                           | Yes   | No                  | Yes  |                  |
[Logstash](https://github.com/pravega?q=logstash)                                | Yes   | No                  | Yes  |                  |
Apache Hadoop                                                                    | Yes   | No                  | Yes  |                  |
[Kafka Adapter](https://github.com/pravega/kafka-adapter)                        | Yes   | No                  | No   |                  | Limited support for Kafka applications that use Java.
[Boomi](https://github.com/pravega/boomi-connector)                              | Yes   | No                  | Yes  |                  |
[Pravega GRPC Gateway](https://github.com/pravega/pravega-grpc-gateway)          | Yes   | Yes                 | Yes  |                  |
[GStreamer](https://github.com/pravega/gstreamer-pravega)                        | Yes   | No                  | Yes  |                  |
[NVIDIA DeepStream Message Broker](https://github.com/pravega/gstreamer-pravega) | Yes   | No                  | No   |                  |
.NET                                                                             | ?     | ?                   | ?    |                  |
Akka                                                                             | ?     | ?                   | ?    |                  |
[Presto](https://github.com/pravega/presto-connector)                            | No    | No                  | Yes  |                  |
Pravega Java API                                                                 | Yes   | Yes                 | Yes  |                  |
Pravega Rust API                                                                 | Yes   | Yes                 | Yes  |                  |
Pravega Python API                                                               | Yes   | Yes                 | Yes  |                  |

**Write**: *Yes* means that the method can write data to Pravega streams. Writes must always append to the tail of a Pravega stream.

**Transactional Write**: *Yes* means that the method can write data to Pravega streams using Pravega transactions. When properly used throughout a stream processing pipeline, this can provide exactly-once semantics.

**Read**: *Yes* means that the method can read data from Pravega streams.

**Special Feature - EO (Exactly-Once)**: Exactly-once semantics means that events are never lost, duplicated, or processed out-of-order. In general, a stream processing pipeline can only offer exactly-once semantics if all components offer exactly-once semantics. This is usually implemented by using transactional writes and stream cuts.

## Pravega Sensor Collector

[Pravega Sensor Collector](https://github.com/pravega/pravega-sensor-collector) is an application that collects data from sensors and ingests the data into Pravega streams.

Pravega Sensor Collector can continuously collect high-resolution samples without interruption, even if the network connection to the Pravega server is unavailable for long periods. For instance, in a connected train use case, there may be long periods of time between cities where there is no network access. During this time, Pravega Sensor Collector will store collected sensor data on a local disk and periodically attempt to reconnect to the Pravega server. It stores this sensor data in local SQLite database files. When transferring samples from a SQLite database file to Pravega, it coordinates a SQLite transaction and a Pravega transaction. This technique allows it to guarantee that all samples are sent in order, without gaps, and without duplicates, even in the presence of computer, network, and power failures.

## SDP Ingest Gateway

SDP Ingest Gateway is a component of **Dell EMC Streaming Data Platform** that provides a REST interface for writing events into Pravega streams. It is designed to receive POST requests from a variety of sources including Dell iDRAC, a management controller card embedded in the motherboard of Dell PowerEdge servers.

## Pravega Search

Pravega Search (PSearch) is a component of **Dell EMC Streaming Data Platform** that provides search functionality against Pravega streams.

<IfMissingFeature feature="nautilus">

For more information, see the [public Streaming Data Platform Developer's Guide](https://dl.dell.com/content/docu96951_Streaming_Data_Platform_Developers_Guide.pdf).

</IfMissingFeature>
<IfHaveFeature feature="nautilus">

For more information, see the [private Streaming Data Platform Developer's Guide](../sdp/developer-guide/guide#working-with-pravega-search-psearch).

</IfHaveFeature>
