#!/bin/bash
sbt it:test
sbt clean coverage compile scalafmtAll test dependencyUpdates coverageReport
