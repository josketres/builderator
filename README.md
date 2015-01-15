# builderator
A simple test data builder generator for Java

[![Build Status](https://travis-ci.org/josketres/builderator.svg?branch=master)](https://travis-ci.org/josketres/builderator)

Usage
---
Use __Builderator__ to generate the source code of a test data builder for a given class.
```java
String source = Builderator.builderFor(Example.class);
System.out.println(source); // save this output as ExampleBuilder.java
```
The example class looks like this:
```java
// Example.java
package test.classes;

public class Example {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```
The generated builder class looks like this:
```java
// ExampleBuilder.java
package test.classes;

import test.classes.Example;

public final class ExampleBuilder { 
    private int age;
    private String name;

    private ExampleBuilder() { }

    public static ExampleBuilder aExample() {
        return new ExampleBuilder();
    }

    public Example build() {
        Example object = new Example();
        object.setAge(age);
        object.setName(name);

        return object;
    }

    public ExampleBuilder age(int age) {
        this.age = age;
        return this;
    }

    public ExampleBuilder name(String name) {
        this.name = name;
        return this;
    }
}
```

Download
--------

Download [the latest JAR][1] or grab via Maven:
```xml
<dependency>
  <groupId>com.josketres</groupId>
  <artifactId>builderator</artifactId>
  <version>1.0.0</version>
  <scope>test</scope>
</dependency>
```
or Gradle:
```groovy
testCompile 'com.josketres:builderator:1.0.0'
```

License
-------

    Copyright 2015 Josué Zarzosa de la Torre

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [1]: https://search.maven.org/remote_content?g=com.josketres&a=builderator&v=LATEST
