#!/bin/sh
rm *.class
javac -classpath /usr/local/activemq/lib/activemq-core-5.2.0.jar:/usr/local/activemq/lib/geronimo-jms_1.1_spec-1.1.1.jar -d . ServerDemo.java
