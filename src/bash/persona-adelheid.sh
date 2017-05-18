#!/bin/bash

I=$1

translatr project ls P$I
PROJECT_ID=$(translatr project create P$I | sed 's/^.*ID: \([^)]*\))$/\1/g')
translatr project ls P$I
translatr locale create --project-id $PROJECT_ID en
translatr locale ls --project-id $PROJECT_ID
translatr key create --project-id $PROJECT_ID key1
translatr key create --project-id $PROJECT_ID key2
translatr key create --project-id $PROJECT_ID key3
translatr key rm --project-id $PROJECT_ID key2
translatr key ls --project-id $PROJECT_ID
translatr locale create --project-id $PROJECT_ID de
translatr locale rm --project-id $PROJECT_ID de
translatr locale ls --project-id $PROJECT_ID
translatr project rm P$I
