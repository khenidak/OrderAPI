#!/bin/bash

# Attempts to read mongo connection string from file @ CONSTR_LOCATION
# if not existing it will attempt to find it in ENV MONG_HOST
# if not existint it will be default a fixed location 


defaultMongoHost='mongodb://web:complexpwd@localhost:27017/ordering'
if [ "$CONSTR_LOCATION" = "" ];then 
 currentmongohost="${MONGO_HOST:-$defaultMongoHost}"
  printf "$CONSTR_LOCATION is empty, using defaulted to: $currentmongohost \n"
else
  if [ -e $CONSTR_LOCATION ];then
    printf "file $CONSTR_LOCATION will be read for mongo connection string\n" 
    currentmongohost=$(cat $CONSTR_LOCATION)
  else
    # we got file name but it was not there
    # let us use the default or env var
   currentmongohost="${MONGO_HOST:-$defaultMongoHost}"
   printf "$CONSTR_LOCATION does not exist, will defailted to $currentmongohost \n"
 fi
fi

# the java code expects MONGO_HOST env
export MONGO_HOST=$currentmongohost 
echo "Will try to connect at : $MONGO_HOST"

java -jar ./build/libs/ordering-*.jar
