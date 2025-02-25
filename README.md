# qu-iam
[![CI](https://github.com/lloydmeta/qu-iam/actions/workflows/ci.yml/badge.svg)](https://github.com/lloydmeta/qu-iam/actions/workflows/ci.yml) 

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

Created using

```sh
quarkus create app com.beachape:qu-iam \
  --extensions=rest,rest-jackson \
  --gradle-kotlin-dsl
```

Uses Java23

## Explored areas

* Quarkus (extensions etc)
* Java 21+ style, features
* BouncyCastle FIPS
* Domain-modelling-style organisation
* Authentication:
  * Creating Users w/ passwords
  * Getting a cookie + JWT back for a given user upon providing ^
  * Returning user info when presented with cookie or bearer auth
* OpenAPI
* o11y:
  * metrics
  * logs
  * tracing
* Creating a native executable

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.additional-build-args='--emit build-report'
```

Or, if you don't have GraalVM installed (`brew install --cask graalvm-jdk` will do it on Mac), you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.additional-build-args='--emit build-report'
```

You can then execute your native executable with: `./build/qu-iam-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

## OpenTelementry

Set the OTel url and secrets in your env, then run, e.g.

```sh
export QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT="${OTEL_EXPORTER_OTLP_URL}"
export QUARKUS_OTEL_EXPORTER_OTLP_HEADERS="authorization=Bearer ${OTEL_EXPORTER_OTLP_SECRET_TOKEN}"
./gradlew quarkusDev
```
