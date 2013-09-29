The Directory Module
====================

This module provides access to the files in a specified directory. Use it for serving static assets.

Here is a full configuration of the module

```
{
    match.prefix = "/"
    driver = "directory"
    config = {

        #The list of files to be tried when accessing a directory path
        index = ["index.html", "index.htm", "index"]

        #The size of the buffer used when reading a file from disk
        buffer-size = 16384

        #Method used to read the file from disk
        #Options are classic (based on InputStream), nio (based on file channels) and async-nio (based on asynchronous file channels)
        data-reader = "async-nio"

        #Provide mappings from file extensions to file types
        mime-type-mapping {
            "html" : "text/html",
            "css"  : "text/css",
            "js"   : "text/javascript",
            "jpg"  : "image/jpeg",
            "jpeg" : "image/jpeg",
            "png"  : "image/png",
            "gif"  : "image/gif"
        }

        #Provide a default file extension
        default-mime-type = "application/octet-stream"
    }
}
```