#!/bin/bash

# Function to show usage of the command
usage() {
    echo "Usage: $0 <NORMALIZE> <mode> <reducers> <opera>"
    echo "NORMALIZE: true for normalizing the text, false otherwise"
    exit 1
}

# Check if the number of arguments is correct
if [ "$#" -ne 4 ]; then
    usage
fi

# Assign arguments to variables
NORMALIZE=$1
MODE=$2
REDUCERS=$3
OPERA=$4

# Define the suffix based on the value of NORMALIZE
if [ "$NORMALIZE" = "true" ]; then
    NORMALIZE_SUFFIX="_normalized"
    NORM_DIR="normalized"
else
    NORMALIZE_SUFFIX=""
    NORM_DIR="not_normalized"
fi

# Define variables with updated paths
JAR_FILE="hadoop-letter-frequency-1.0-SNAPSHOT.jar"
CLASS_NAME="it.unipi.tonystark.MapReduceApp"

OUTPUT_BASE="output/qualitative_analysis/$OPERA"

OUTPUT_COUNT="$OUTPUT_BASE/count/$NORM_DIR/${MODE}_${REDUCERS}"
OUTPUT_FREQ="$OUTPUT_BASE/frequency/$NORM_DIR/${MODE}_${REDUCERS}"

LOG_DIR="output/performance_analysis/$OPERA/${REDUCERS}_reducer/$MODE"
LOG_FILE="$LOG_DIR/${OPERA}${NORMALIZE_SUFFIX}.txt"

# Create the log directory if it does not exist
mkdir -p "$LOG_DIR"

# Execute the Hadoop command and redirect the output to the log file
hadoop jar "$JAR_FILE" "$CLASS_NAME" "$MODE" "$REDUCERS" "$NORMALIZE" "inputFiles/${OPERA}.txt" "$OUTPUT_COUNT" "$OUTPUT_FREQ" > "$LOG_FILE" 2>&1

# Check if the command was executed successfully
if [ $? -eq 0 ]; then
    echo "Command executed successfully. Log saved in $LOG_FILE."
else
    echo "Error executing the command. Check the log in $LOG_FILE for more details."
fi
