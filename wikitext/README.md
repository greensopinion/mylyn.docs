# Building

Building WikiText is a little unusual due to the use of a split build involving
regular Maven and Tycho.

## Building All Components

Run `mvn -f build-pom.xml verify` to build both core and ui components.

To specify different goals, define the wikitext.goals property as follows:

`mvn -f build-pom.xml verify -Dwikitext.goals=clean,deploy`

## Building Just Core components

From the `core` folder, simply run maven as usual, e.g.:

`mvn clean verify`
