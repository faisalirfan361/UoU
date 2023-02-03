#!/bin/bash
# Helper to create kafka topics and create/update schemas in confluent cloud.
# This can be run multiple times since topics will not be recreated, and schemas will be updated
# only when something has changed (schema registry determines that).
#
# You must have access to confluent cloud and configure your environment as shown below.
# For the schema registry, you'll need a valid API key (generated in confluent cloud).
#
# Requirements:
# - confluent-cli (brew install confluent-cli)
# - curl (brew install curl)
# - jq (brew install jq)
#
# Setup your environment:
# confluent login
# confluent environment use {env-id}
# confluent kafka cluster use {cluster-id}
# export SCHEMA_REGISTRY_URL={url}
# export SCHEMA_REGISTRY_AUTH={apiKey}:{apiSecret}

set -e
set -u

# Config
# Keep the retry count and naming suffixes in sync with our Spring settings.
rootDir="$(cd $(dirname "$0")/.. && pwd)"
schemaDir="$rootDir/src/main/avro"
defaultAttempts=3
retrySuffix="--calendars.retry-" # retry number will be added to end
dltSuffix="--calendars.dlt"

function main() {
  confirmEnvironment

  # flags
  flags=""
  flags="--dryrun" # comment out to create topics for real

  # for qa:
  partitions=6
  config='replication=3'

  # for prod:
  # partitions=12
  # config='replication=3'

  # Create the topics. Keep names and retry counts in sync with our Spring settings.
  # Tasks:
  createTopicGroup $flags 'calendars.tasks.update-all-subaccount-tokens' "$defaultAttempts" "$partitions" "$config" 'tasks/UpdateAllSubaccountTokens.avsc'
  createTopicGroup $flags 'calendars.tasks.update-subaccount-token' "$defaultAttempts" "$partitions" "$config" 'tasks/UpdateSubaccountToken.avsc'
  createTopicGroup $flags 'calendars.tasks.update-account-sync-state' "$defaultAttempts" "$partitions" "$config" 'tasks/UpdateAccountSyncState.avsc'
  createTopicGroup $flags 'calendars.tasks.delete-account-from-nylas' "$defaultAttempts" "$partitions" "$config" 'tasks/DeleteAccountFromNylas.avsc'
  createTopicGroup $flags 'calendars.tasks.import-all-calendars-from-nylas' "$defaultAttempts" "$partitions" "$config" 'tasks/ImportAllCalendarsFromNylas.avsc'
  createTopicGroup $flags 'calendars.tasks.export-calendars-to-nylas' "$defaultAttempts" "$partitions" "$config" 'tasks/ExportCalendarsToNylas.avsc'
  createTopicGroup $flags 'calendars.tasks.change-calendar' "$defaultAttempts" "$partitions" "$config" 'tasks/ChangeCalendar.avsc'
  createTopicGroup $flags 'calendars.tasks.sync-all-events' "$defaultAttempts" "$partitions" "$config" 'tasks/SyncAllEvents.avsc'
  createTopicGroup $flags 'calendars.tasks.change-event' "$defaultAttempts" "$partitions" "$config" 'tasks/ChangeEvent.avsc'
  createTopicGroup $flags 'calendars.tasks.maintenance' 2 "$partitions" "$config" 'tasks/Maintenance.avsc'
  createTopicGroup $flags 'calendars.tasks.diagnostics' 1 "$partitions" "$config" 'tasks/Diagnostics.avsc'

  # Events:
  createTopicGroup $flags 'calendars.events.event-changed' "$defaultAttempts" "$partitions" "$config" 'events/EventChanged.avsc'

  # Public events:
  createTopic $flags 'calendars.public.events.event-changed' $partitions "$config" 'publicevents/EventChanged.avsc'
}

function confirmEnvironment() {
  echo "Confluent environment:"
  confluent environment list | grep '*' | sed -e 's/^[[:space:]]*//'
  echo ""
  echo "Confluent cluster:"
  confluent kafka cluster list | grep '*' | sed -e 's/^[[:space:]]*//'
  echo ""
  echo "Schema registry:"
  echo "$SCHEMA_REGISTRY_URL"
  echo ""
  echo "Schema registry subjects (first 10):"
  curl --silent -u "$SCHEMA_REGISTRY_AUTH" "$SCHEMA_REGISTRY_URL/subjects" \
    | jq -r 'limit(10; .[]) | "- " + .'
  echo ""

  read -rp "Is the confluent environment shown above correct? Proceed? [y/n] " -n1 yn
  echo ""
  if [[ "$yn" != [yY] ]]; then
    exit 0
  fi
  echo ""
}

# Creates a single topic and schema.
# Usage: createTopic [--approve] [--dryrun] <topic> <partitions> <config> <schemaName>
function createTopic() {
  autoApprove=""
  [ "$1" == "--approve" ] && autoApprove=1 && shift
  dryRun=""
  [ "$1" == "--dryrun" ] && dryRun=1 && shift
  topic="$1"
  partitions="$2"
  config="$3"
  schemaName="$4"

  schemaPath="$schemaDir/$schemaName"
  schemaJson=$(jq '{"schema": (. | tostring) }' -c < "$schemaPath")
  schemaUrl="$SCHEMA_REGISTRY_URL/subjects/$topic-value/versions"

  existingTopic=$(confluent kafka topic describe "$topic" --output json 2> /dev/null \
      | jq '.topic_name' || echo '')

  echo ""
  [ -n "$dryRun" ] && echo "!! DRY RUN !!"
  echo "---- Topic details ----"
  echo "Topic: $topic"
  if [[ -n "$existingTopic" ]]; then
    echo "Exists: YES (will not be created)"
  else
    echo "Exists: NO (will be created)"
    echo "Create params: partitions=$partitions, $config"
  fi;
  echo "Schema path: $schemaPath"
  echo "Schema content: $schemaJson"
  echo "Schema POST URL: $schemaUrl"
  echo ""

  if [ -z "$autoApprove" ]; then
    read -rp "Are you sure you want to proceed? [y/n] " -n1 yn
    echo ""
    if [[ "$yn" != [yY] ]]; then
      return
    fi
  fi

  if [[ -z "$existingTopic" ]]; then
    echo "Creating topic..."
    [ -n "$dryRun" ] || confluent kafka topic create "$topic" --partitions "$partitions" --config "$config"
  else
    echo "Topic already exists. Skipping create."
  fi

  # Note: You can POST an existing schema, and if the content is exactly the same as the current,
  # version, it will just return the current version id. Because the schema registry is shared
  # by environment clusters (dev/prod), we will usually run into this when creating prod topics.
  # The registry will also re-use the same id across topics if the schemas match exactly.
  echo "Creating/updating schema..."
  [ -n "$dryRun" ] || curl -X POST --silent \
    -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    -u "$SCHEMA_REGISTRY_AUTH" --data "$schemaJson" "$schemaUrl" \
    | jq -r '"Created schema: " + (.id | tostring)'
}

# Creates a group of topics with retry and dlt (dead letter) topics.
# Usage: createTopicGroup [--approve] [--dryrun] <baseTopic> <retries> <partitions> <config> <schemaName>
function createTopicGroup() {
  autoApprove=""
  [ "$1" == "--approve" ] && autoApprove=1 && shift
  dryRun=""
  [ "$1" == "--dryrun" ] && dryRun=1 && shift
  topic="$1"
  attempts=$(($2)) # ensure number
  retries=$((attempts - 1))
  partitions="$3"
  config="$4"
  schemaName="$5"

  allTopics=($topic)
  if [ $retries -gt 0 ]; then
    for n in $(seq $retries); do
      allTopics+=("${topic}${retrySuffix}$((n-1))")
    done;
  fi;
  allTopics+=("${topic}${dltSuffix}")

  printf -v allTopicsDisplay '%s\n' "${allTopics[@]}"

  schemaPath="$schemaDir/$schemaName"
  schemaJson=$(jq '{"schema": (. | tostring), "schemaType": "AVRO" }' -c < "$schemaPath")
  schemaUrl="$SCHEMA_REGISTRY_URL/subjects/$topic-value/versions?normalize=true"

  echo ""
  [ -n "$dryRun" ] && echo "!! DRY RUN !!"
  echo "---- Topic group ----"
  echo "$allTopicsDisplay"
  echo "Partitions: $partitions"
  echo "Config: $config"
  echo "Schema path: $schemaPath"
  echo "Schema content: $schemaJson"
  echo "Schema POST URL (base topic): $schemaUrl"
  echo ""

  if [ -z "$autoApprove" ]; then
    read -rp "Are you sure you want to create the group of topics? [y/n] " -n1 yn
    echo ""
    if [[ "$yn" != [yY] ]]; then
      return
    fi
  fi

  createTopicFlags="--approve"
  [ -n "$dryRun" ] && createTopicFlags="$createTopicFlags --dryrun"

  for currentTopic in "${allTopics[@]}"; do
    createTopic $createTopicFlags "$currentTopic" "$partitions" "$config" "$schemaName"
  done;
}

# Entry point:
main
