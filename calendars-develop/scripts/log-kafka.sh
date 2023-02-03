#!/bin/bash
# Streams kafka messages to a log file for development and debugging.
# All params are optional, and by default this will connect to localhost kafka since that is the
# primary use case. Non-local clusters will require extra kafka security/sasl params for kcat.
#
# Get help:
# ./log-kafka.sh -h
#
# Requirements:
# - kcat (brew install kcat)
# - jq (brew install jq)

set -e
set -u

rootDir="$(cd $(dirname "$0")/.. && pwd)"
topic=calendars.public.events.event-changed
group="calendars-test-$(hostname -s | tr -Cd '[:alnum:]')"
output="$rootDir/logs/kafka.json"
kafka=localhost:9092
schemaRegistry=http://localhost:8081
format='{"%o": %T, "v": %s}\n'
help="Usage: ./$(basename $0) <options>

Options:
  -t <topic>  Kafka topic - default: $topic
  -g <group>  Kafka consumer group - default: $group
  -o <output> Output file path - default: $output
  -k <kafka>  Kafka host - default: $kafka
  -X <arg>    Extra kafka config args to pass directly to kcat (can be repeated)
  -s <url>    Schema registry URL - default: $schemaRegistry
  -f <format> Format string for kcat output - default: $format
  -h          Show this help

Examples:
./$(basename $0)
./$(basename $0) -t some-topic -o logs/log.json
./$(basename $0) -k example.confluence.cloud -X security.protocol=sasl_ssl -X sasl.mechanisms=PLAIN \\
  -X sasl.user=user -X sasl.password=pass -s https://user:pass@example.confluent.cloud
"

while getopts t:g:o:k:X:s:f:h opt; do
  case "$opt" in
    t) topic="$OPTARG" ;;
    g) group="$OPTARG" ;;
    o) output="$OPTARG" ;;
    k) kafka="$OPTARG" ;;
    X) extra+=("-X $OPTARG") ;;
    s) schemaRegistry="$OPTARG" ;;
    f) format="$OPTARG" ;;
    h) echo "$help"; exit 0 ;;
    ?) echo "$help"; exit 1 ;;
  esac
done

echo "Streaming from kafka ($kafka) '$topic' to '$output' (Ctrl+C to exit)"
kcat -b "$kafka" -r "$schemaRegistry" -u -s value=avro -o end -f "$format" ${extra[@]-} -G "$group" "$topic" \
    | tee -a "$output" \
    | jq -c
