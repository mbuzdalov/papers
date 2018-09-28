#!/bin/bash

java -Xmx1G -cp ../target/scala-2.12/classes:/usr/share/scala-2.12/lib/scala-library.jar onell.MainTuning "$@"
