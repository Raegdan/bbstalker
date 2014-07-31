#!/bin/bash

## BBStalker colors picking tool for reverse search database
## By Raegdan for the BBStalker project

for i in *; do
	viewnior $i &
	pid=$!
	echo $i >> colors.log
	grabc >> colors.log
	grabc >> colors.log
	echo >> colors.log
	kill -9 $pid
done
