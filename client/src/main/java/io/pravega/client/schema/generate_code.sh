#!/usr/bin/env bash
AVRO_TOOLS=../../../../../../../../../avro/avro-tools-1.9.1.jar
java -jar ${AVRO_TOOLS} compile schema User.avsc ../../../..
java -jar ${AVRO_TOOLS} compile schema SensorData.avsc ../../../..
