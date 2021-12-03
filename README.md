THIS PROJECT IS ARCHIVED

The current users likely have their own fork somewhere but this one is unmaintained.

# lt-xml
Automatically exported from code.google.com/p/lt-xml

In working to optimize startup time for embedded devices running Java it was found that the 
JAXB xml/java binding framework was creating thousands of classes at runtime which added tens
of seconds to the boot time of the device.  Through testing, we found that using reflection
was much less impact on boot time without impacting runtime performance, hence this "light xml" 
binding framework was created.
