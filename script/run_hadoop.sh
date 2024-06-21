#!/bin/bash

# Function to show usage of the command
usage() {
    echo "Usage: $0 <ACCENT> <mode> <reducers> <opera>"
    echo "ACCENT: true for No_Accents, false for Accents"
    exit 1
}

# Check if the number of arguments is correct
if [ "$#" -ne 4 ]; then
    usage
fi

# Assign arguments to variables
ACCENT=$1
MODE=$2
REDUCERS=$3
OPERA=$4

# Define the suffix based on the value of ACCENT
if [ "$ACCENT" = "true" ]; then
    ACCENT_SUFFIX="No_Accents"
else
    ACCENT_SUFFIX="Accents"
fi

# Define variables
JAR_FILE="hadoop-letter-frequency-1.0-SNAPSHOT.jar"
CLASS_NAME="it.unipi.tonystark.MapReduceApp"
INPUT_PATH="inputFiles/${OPERA}*.txt"
OUTPUT_COUNT="output/${OPERA}/count${OPERA}_${ACCENT_SUFFIX}_${MODE}_${REDUCERS}"
OUTPUT_FREQ="output/${OPERA}/freq${OPERA}_${REDUCERS}_reducer_${ACCENT_SUFFIX}_${MODE}"
LOG_DIR="performance/${OPERA}/${REDUCERS}_reducer/${MODE}"
LOG_FILE="${LOG_DIR}/performance${OPERA}_${REDUCERS}_reducer_${ACCENT_SUFFIX}_${MODE}.txt"

# Create the log directory if it does not exist
mkdir -p $LOG_DIR

# Execute the Hadoop command and redirect the output to the log file
hadoop jar $JAR_FILE $CLASS_NAME $MODE $REDUCERS $ACCENT $INPUT_PATH $OUTPUT_COUNT $OUTPUT_FREQ > $LOG_FILE 2>&1

# Check if the command was executed successfully
if [ $? -eq 0 ]; then
    echo "Command executed successfully. Log saved in $LOG_FILE."
else
    echo "Error executing the command. Check the log in $LOG_FILE for more details."
fi
