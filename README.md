# easyPie

### Operation transformation based collaborative editor

## Modules structure
```
shared ----> core ----> database ----> api
       \
        \----> client
```
* ```shared``` Contains DTOs (websocket messages) and Operation Transformation methods with abstractions.
* ```core``` Contains core data types such as Account, Document, Permissions, etc.
* ```database``` Contains repositories interfaces and mongoDB implementations.
* ```api``` Consist of two parts. The first one is HTTP REST api for basic interaction with service such as authentication, document creation and sharing. 
            The second one includes web socket API for synchronization of documents.
* ```client``` Is a ScalaJS nano library for interaction with websocket server's api part. 
* ```app``` Simple example of client library usage in simple js application.
