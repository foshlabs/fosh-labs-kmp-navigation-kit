#!/usr/bin/env bash
# Wrapper that loads gradle-secrets.properties and runs publishToMavenCentral.
# The vanniktech plugin reads credentials via providers.gradleProperty() which only
# sees gradle.properties and ORG_GRADLE_PROJECT_* env vars, not ext properties.
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SECRETS_FILE="$SCRIPT_DIR/gradle-secrets.properties"
if [[ ! -f "$SECRETS_FILE" ]]; then
    echo "Error: gradle-secrets.properties not found. Create it with ossrhUsername, ossrhPassword, signing.key, signing.keyId, signing.password."
    exit 1
fi
get_prop() {
    local key=$1
    local fallback=$2
    local val
    val=$(grep "^${key}=" "$SECRETS_FILE" 2>/dev/null | cut -d= -f2- | head -1 | tr -d '\r')
    if [[ -n "$val" ]]; then
        echo "$val"
    elif [[ -n "$fallback" ]]; then
        val=$(grep "^${fallback}=" "$SECRETS_FILE" 2>/dev/null | cut -d= -f2- | head -1 | tr -d '\r')
        echo "$val"
    fi
}
# Maven Central credentials
ORG_GRADLE_PROJECT_mavenCentralUsername=$(get_prop mavenCentralUsername ossrhUsername)
ORG_GRADLE_PROJECT_mavenCentralPassword=$(get_prop mavenCentralPassword ossrhPassword)
# PGP signing (signing.key has \n for newlines in properties; convert to real newlines)
SIGNING_KEY=$(get_prop signingInMemoryKey signing.key)
if [[ -n "$SIGNING_KEY" ]]; then
    SIGNING_KEY="${SIGNING_KEY//\\n/$'\n'}"
fi
ORG_GRADLE_PROJECT_signingInMemoryKey="$SIGNING_KEY"
ORG_GRADLE_PROJECT_signingInMemoryKeyId=$(get_prop signingInMemoryKeyId signing.keyId)
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=$(get_prop signingInMemoryKeyPassword signing.password)

if [[ -z "$ORG_GRADLE_PROJECT_mavenCentralUsername" || -z "$ORG_GRADLE_PROJECT_mavenCentralPassword" ]]; then
    echo "Error: Could not find mavenCentralUsername/ossrhUsername and mavenCentralPassword/ossrhPassword in gradle-secrets.properties"
    exit 1
fi
if [[ -z "$ORG_GRADLE_PROJECT_signingInMemoryKey" || -z "$ORG_GRADLE_PROJECT_signingInMemoryKeyId" ]]; then
    echo "Error: Could not find signing.key and signing.keyId in gradle-secrets.properties"
    exit 1
fi

export ORG_GRADLE_PROJECT_mavenCentralUsername
export ORG_GRADLE_PROJECT_mavenCentralPassword
export ORG_GRADLE_PROJECT_signingInMemoryKey
export ORG_GRADLE_PROJECT_signingInMemoryKeyId
# Password may be empty; Gradle's signing plugin accepts that
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${ORG_GRADLE_PROJECT_signingInMemoryKeyPassword:-}"
exec "$SCRIPT_DIR/gradlew" publishToMavenCentral "$@"
