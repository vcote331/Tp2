#!/usr/bin/env bash

find . -name "*.java" -print | xargs javac -cp "./dependance/*"
find . -name "*.py" -print | xargs python -m "./dependance/*"
