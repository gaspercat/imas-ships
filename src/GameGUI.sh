#!/bin/bash
java -cp "./bin:./lib:./lib/Base64.jar:./lib/iiop.jar:./lib/jade.jar:./lib/jadeTools.jar:./lib/commons-codec-1.3.jar"  -Xmx400m -Xms400m jade.Boot -nomtp -gui central:sma.CentralAgent;coord:sma.CoordinatorAgent
