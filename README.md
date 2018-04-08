# Spring Boot Auto-Configuration for the Solace JMS

This project provides Spring Boot Auto-Configuration and an associated Spring Boot Started for the Solace JMS API. The goal of this project is to make it easy to use Solace JMS within a Spring application so you can take advantage of all the benefits of Spring Boot auto-configuration.

For a high level introduction and explanation, you can also refer to the following blog post: https://solace.com/blog/devops/solace-jms-meet-spring-boot-starters

## Contents

* [Overview](#overview)
* [Using Auto-Configuration in your App](#using-auto-configuration-in-your-app)
* [Building the Project Yourself](#building-the-project-yourself)
* [Running the Sample](#running-the-sample)
* [Contributing](#contributing)
* [Authors](#authors)
* [License](#license)
* [Resources](#resources)

---

## Overview
 
As stated this project provides a Spring Boot Auto-Configuration implementation and a Spring Boot Starter pom for the Solace JMS API. The goal of this project is to make it easier to use Solace JMS within Spring.  

The artifacts are published to Maven Central so it should be familiar and intuitive to use this project in your applications. Currently this project is still experimental and as such you may find that many Solace JMS properties are not yet supported. If you find Solace JMS properties that this project does not yet support, simply raise an issue and we'll look into adding this support or submit a pull request with the update.

    
## Using Auto-Configuration in your App

Spring Boot Auto-Configuration for the Solace JMS supports both programmatic creation or JNDI lookup of JMS objects. To learn more about JNDI refer to the [Obtaining JMS objects using JNDI tutorial](https://solacesamples.github.io/solace-samples-jms/using-jndi/).

#### Programmatic creation of JMS objects

See the associated `solace-jms-sample-app` for an example of how this is all put together in a simple application. To use Solace JMS you need to do these steps:

1. Update your build.
2. Autowire the `ConnectionFactory`.
3. Configure the application to use a Solace Messaging service.

#### JNDI lookup of JMS objects

See the associated `solace-jms-sample-app-jndi` for an example. To use JNDI with Solace JMS you need to do these steps:

1. Update your build.
2. Autowire the `JndiTemplate` for further use e.g.: in a `JndiObjectFactoryBean`.
3. Configure the application to use a Solace Messaging service.


### Updating your build

This releases from this project are hosted in [Maven Central](https://mvnrepository.com/artifact/com.solace.spring.boot/solace-jms-spring-boot-starter)

The easiest way to get started is to include the `solace-jms-spring-boot-starter` in your application. For an examples see the [JMS Sample App](https://github.com/SolaceProducts/solace-jms-spring-boot/tree/master/solace-jms-sample-app) in this project.

Here is how to include the spring boot starter in your project using Gradle and Maven.

#### Using it with Gradle

```groovy
compile("com.solace.spring.boot:solace-jms-spring-boot-starter:1.+")
```

#### Using it with Maven

```xml
<dependency>
	<groupId>com.solace.spring.boot</groupId>
	<artifactId>solace-jms-spring-boot-starter</artifactId>
	<version>1.+</version>
</dependency>
```
### Configure the Application to use your Solace Messaging Service Credentials
#### Deploying your Application to a Cloud Platform

By using [Spring Cloud Connectors](https://cloud.spring.io/spring-cloud-connectors/), this library can automatically configure a `ConnectionFactory` and/or a `JndiTemplate` using the detected Solace Messaging services when deployed on a Cloud Platform such as Cloud Foundry.

Currently, the [Solace Cloud Foundry Cloud Connector](https://github.com/SolaceProducts/sl-spring-cloud-connectors) is the only connector that is supported by default in this library, but could easily be augmented by adding your own Solace Spring Cloud Connectors as dependencies to the [auto-configuration's POM](solace-jms-spring-boot-autoconfigure/pom.xml).

For example:

```xml
<dependency>
	<groupId>com.solace.cloud.cloudfoundry</groupId>
	<artifactId>solace-spring-cloud-connector</artifactId>
	<version>2.1.0</version>
</dependency>
```

#### Exposing a Solace Messaging Service Manifest in the Application's Environment

Configuration of the `ConnectionFactory` and/or the `JndiTemplate` can be done through exposing a Solace Messaging service manifest to the application's JVM properties or OS environment.

For example, you can set a `SOLCAP_SERVICES` variable in either your JVM properties or OS's environment to directly contain a `VCAP_SERVICES`-formatted manifest file. In which case, the autoconfigure will pick up any Solace Messaging services in it and use them to accordingly configure your `JmsTemplate`.

The properties provided by this externally-provided manifest can also be augmented using the values from the [application's properties file](#updating-your-application-properties).

For details on valid manifest formats and other ways of exposing Solace service manifests to your application, see the [Manifest Load Order and Expected Formats](https://github.com/SolaceProducts/solace-services-info#manifest-load-order-and-expected-formats) section in the [Solace Services Info](https://github.com/SolaceProducts/solace-services-info) project.

#### Updating your Application Properties

Alternatively, configuration of the `JmsTemplate` can also be entirely done through the `application.properties` file. This is where users can control the Solace JMS API properties. Currently this project supports direct configuration of the following properties:

```
solace.jms.host
solace.jms.msgVpn
solace.jms.clientUsername
solace.jms.clientPassword
# Following properties do not apply when using JNDI, see below.
solace.jms.clientName
solace.jms.directTransport 
```

Where reasonable, sensible defaults are always chosen. So a developer using a Solace VMR and wishing to use the default message-vpn must only set the `solace.jms.host`. When using JNDI, the configured connection factory properties on the Solace message router are taken as a starting point, including the `clientName` and `directTransport` configurations.

See [`SolaceJmsProperties`](https://github.com/SolaceProducts/solace-jms-spring-boot/blob/master/solace-jms-spring-boot-autoconfigure/src/main/java/com/solace/spring/boot/autoconfigure/SolaceJmsProperties.java) for the most up to date list of directly configurable properties.

Any additional Solace JMS API properties can be set through configuring `solace.jms.apiProperties.<Property>` where `<Property>` is the name of the property as defined in the [Solace JMS API documentation for `com.solacesystems.jms.SupportedProperty`](https://docs.solace.com/API-Developer-Online-Ref-Documentation/jms/constant-values.html ), for example:

```
solace.jms.apiProperties.SOLACE_JMS_SSL_TRUST_STORE=ABC
```

Note that the direct configuration of `solace.jms.` properties takes precedence over the `solace.jms.apiProperties.`.

## Building the Project Yourself 

This project depends on maven for building. To build the jar locally, check out the project and build from source by doing the following:

    git clone https://github.com/SolaceProducts/solace-jms-spring-boot.git
    cd solace-jms-spring-boot
    mvn package

This will build the auto-configuration jar and associated sample.

Note: As currently setup, the build requires Java 1.8. If you want to use another older version of Java adjust the build accordingly.

## Running the Sample 

The simplest way to run the sample is from the project root folder using maven. For example:

	cd solace-jms-sample-app
    mvn spring-boot:run

or

	cd solace-jms-sample-app-jndi
    mvn spring-boot:run

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

See the list of [contributors](https://github.com/SolaceProducts/solace-jms-spring-boot/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

## Resources

For more information about Spring Boot Auto-Configuration and Starters try these resources:

- [Spring Docs - Spring Boot Auto-Configuration](http://docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#using-boot-auto-configuration)
- [Spring Docs - Developing Auto-Configuration](http://docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#boot-features-developing-auto-configuration)
- [GitHub Tutorial - Master Spring Boot Auto-Configuration](https://github.com/snicoll-demos/spring-boot-master-auto-configuration)

For more information about Solace technology in general please visit these resources:

- The Solace Developer Portal website at: http://dev.solace.com
- Understanding [Solace technology.](http://dev.solace.com/tech/)
- Ask the [Solace community](http://dev.solace.com/community/).
