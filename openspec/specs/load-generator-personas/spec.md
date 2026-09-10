# load-generator-personas Specification

## Purpose
Defines the externally observable behavior of the persona-driven load generator (`ui/libs/generator`) when its personas apply reversible "suffix toggle" mutations to Translatr entities, so that real data — where optional string fields are frequently unset — does not crash a generator run.

## Requirements

### Requirement: Suffix-toggle mutations tolerate absent source fields

Persona and helper mutations that reversibly toggle a trailing marker on a string field (append the marker when absent, strip it when present) SHALL treat a source field that is `null`, `undefined`, or empty as the empty string. Such a mutation SHALL NOT throw when the field it reads is unset, and SHALL produce a well-formed update payload whose target field equals the marker (for an append) or the empty string (for a strip).

This applies to every field a persona toggles that is optional on its model, including at minimum:

- a project's `description` (project "update" persona)
- a message's `value` (message update helper)
- a key's `name` (key "update" persona)
- an access token's `name` (access-token "update" persona)
- a locale's `name` (locale "update" persona)

#### Scenario: Updating a project that has no description

- **WHEN** the project "update" persona selects a project whose `description` is unset
- **THEN** it issues a project update with `description` set to the toggle marker
- **AND** no `TypeError` is raised and the persona emits its normal "project updated" result

#### Scenario: Updating a project that already has a description

- **WHEN** the project "update" persona selects a project whose `description` is `"Generated"`
- **THEN** it issues a project update with `description` set to `"Generated!"`
- **AND** selecting the same project again strips the marker back to `"Generated"`

#### Scenario: Toggling any optional field that is unset

- **WHEN** a persona toggles an optional string field (message value, key name, access-token name, or locale name) whose current value is `null`, `undefined`, or empty
- **THEN** the resulting update payload carries that field as the toggle marker for that persona
- **AND** the run continues without an unhandled error in the generator logs
