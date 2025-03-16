# qu-iam
[![CI](https://github.com/lloydmeta/qu-iam/actions/workflows/ci.yml/badge.svg)](https://github.com/lloydmeta/qu-iam/actions/workflows/ci.yml)

This project uses Quarkus with Java 23.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Exploring the application in dev mode

Ensure you have Java 23 installed and set as the "current" Java version.

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

* When that is running, hit `r` to start live **continuous testing** that runs all tests in a loop or `f` to only run failed tests (see [other options](https://quarkus.io/guides/continuous-testing#controlling-continuous-testing))
* **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.
* Play around with the REST API using the OpenAPI UI, which you can navigate from the Dev UI, but can also find at <http://localhost:8080/q/swagger-ui/>.
  * This was integrated using the [Quarkus OpenAPI extension](https://quarkus.io/extensions/io.quarkus/quarkus-smallrye-openapi/) (based on an implementation of [the MicroProfile OpenAPI spec](https://github.com/smallrye/smallrye-open-api)) following [this guide](https://quarkus.io/guides/openapi-swaggerui)
* Look around at [other Quarkus Extensions](https://quarkus.io/extensions/); another one already used in the app is the [OpenTelemetry one](https://quarkus.io/extensions/io.quarkus/quarkus-opentelemetry/); see the [related section below](#opentelementry) for how to run the app while exporting traces to your Otel server.


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

You can create a native executable if you have GraalVM installed (`brew install --cask graalvm-jdk` will do it on Mac) using:

```shell script
./gradlew build \
  -Dquarkus.native.enabled=true \
  -Dquarkus.package.jar.enabled=false \
  -Dquarkus.native.additional-build-args='--emit build-report'
```

Or, if you don't and are targeting Linux, you can run the native executable build in a container using:

```shell script
./gradlew build \
  -Dquarkus.native.enabled=true \
  -Dquarkus.package.jar.enabled=false \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-23
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

Set the OTel url and secrets in your env, then run the app (`OTEL_EXPORTER_OTLP_URL` can be the URL of your Elastic APM server for instance):

```sh
export QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT="${OTEL_EXPORTER_OTLP_URL}"
export QUARKUS_OTEL_EXPORTER_OTLP_HEADERS="authorization=Bearer ${OTEL_EXPORTER_OTLP_SECRET_TOKEN}"
./gradlew quarkusDev
```

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
* [Lombok](https://projectlombok.org) for fluent data class builders
* [MapStruct](https://mapstruct.org) for compile-time class mapping
* Creating a native executable


## Created using

```sh
quarkus create app com.beachape:qu-iam \
  --extensions=rest,rest-jackson \
  --gradle-kotlin-dsl
```

