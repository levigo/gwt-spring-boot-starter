# A Spring-Boot Starter for Google Web Toolkit (GWT) applications

## Features
- integrate GWT compilation into the Spring application lifecycle 
- automatically register and deploy GWT-RPC endpoints
- ~~transparently support GWT dev-mode without IDE-plugins etc.~~
  - with Spring Boot 3 and the migration to JakartaEE, dev mode is no longer supported as part of this project. 
    this might change when gwt-dev has support for JakartaEE.

## Usage
__Maven dependency__

    <dependency>
        <groupId>org.jadice.gwt.spring</groupId>
        <artifactId>gwt-spring-boot-starter</artifactId>
        <version>2.2.1</version>
    </dependency>

If you plan to use the devmode with Java 11 and above, you must supply the following JVM argument:
`--add-opens java.base/jdk.internal.loader=ALL-UNNAMED`

## License
This library is provided "as is" under the "three-clause BSD license". See [LICENSE.md](./LICENSE.md).
