#!/bin/bash
java -cp "./bin:./lib:./lib/Base64.jar:./lib/iiop.jar:./lib/jade.jar:./lib/jadeTools.jar:./lib/commons-codec-1.3.jar" jade.Boot -nomtp central:sma.CentralAgent;coord:sma.CoordinatorAgent
