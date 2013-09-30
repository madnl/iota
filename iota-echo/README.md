Echo Driver
===========

Toy driver to showcase how custom application logic can be implemented in the Iota Server.

This driver has no configuration. Just include it in the modules list:

```
{
    match.prefix = "/echo"
    driver.class = "org.madnl.iota.module.echo.EchoDriverProvider",
}
```