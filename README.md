Iota HTTP Server
================

Iota is a simple, extensible, lightweight HTTP server.

The following features are included:

* module system to plug in custom application logic
* an asset serving module - for static assets
* virtual hosts, on multiple network interfaces
* HTTP persistent connections, including pipelineing

The server is built on top of the Akka IO Layer: http://doc.akka.io/docs/akka/2.2.0/scala/io.html

It can be embedded in an application, or started as stand-alone.

Iota is built using SBT and requires Java 7 to compile & run.

To build download an 0.12.x version of SBT. Clone the project and enter in the `iota` directory.

Run the following commands to build the server

```
sbt "project iota-server" one-jar
cp iota-server/target/scala-2.10/iota-server_2.10-0.1-SNAPSHOT-one-jar.jar ~/my/desired/location
```

This creates a runnable jar. To start the server run:

```
java -jar iota-server_2.10-0.1-SNAPSHOT-one-jar.jar
```

The server expects to find a configuration file named `iota.conf` in the current directory, otherwise an exception
will be displayed. If you wish to provide another location for this configuration file run with

```
java -Diota.configFile=/my/config/path -jar iota-server_2.10-0.1-SNAPSHOT-one-jar.jar
```


Below is a config file example. The flexible Typesafe Config library is used for config files. Take a
look at their project page to see what is the acceptable syntax: https://github.com/typesafehub/config

```
iota {

    # listen on all interfaces on port 90000
    listen = ["*:9000"]

    # don't log anything
    # Also acceptable
    # log.device = stdout
    # log.file = /my/logging/file
    log.device = none

    # the list of served virtual hosts
    sites {
        "test" {
            host = "localhost:9000"

            # requests not specifying a host will default to this site
            default = true

            # list of modules for this site
            modules = [
                {
                    # Specify which URLs should this driver handle
                    # also acceptable
                    # match.exact = /exact/path
                    # match.regex = java-regex
                    match.prefix = "/"

                    # Indicate which driver should handle these requests. Drivers implement
                    # the actual functionality. They are the application part of the server.
                    # The "directory" driver serves static assets
                    driver = "directory"

                    # Each driver can have a specific configuration. The directory
                    # driver, in particular, requires at least a directory path from where
                    # to serve files. Other settings are available, check the driver's
                    # description
                    config.path = "sample-site"
                },
                {
                    match.prefix = "/echo"

                    # drivers can also be specified using a FQCN. The class must be on
                    # the server's classpath.
                    # This driver in particular, echoes the request's details in a HTML page
                    driver.class = "com.adobe.iota.module.echo.EchoDriverProvider"
                }
            ]
        }

        mysite {
            host = "mysite.com:9000"
            modules = [
                {
                    driver.class = "com.adobe.iota.module.echo.EchoDriverProvider"
                }
            ]
        }
    }
}
```

To see what can be configured take a look at the README files for each included driver and at the reference default
configuration at [reference.conf](iota-server/src/main/resources/reference.conf)

The server provides by default 2 modules

* directory - serve static assets
* echo - echo back the request details in a HTML page

Other modules can be created by developing against the API specified in the `iota-api` project.


