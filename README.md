# builderator
A simple test data builder generator for Java

[![Build Status](https://travis-ci.org/josketres/builderator.svg?branch=master)](https://travis-ci.org/josketres/builderator)

Usage
---
Use __Builderator__ to generate the source code of a test data builder for a given class.
```java

// Generates the source code for a test data builder for the Example class
String source = Builderator.builderFor(Example.class);
System.out.println(source); // save this output as ExampleBuilder.java

// Example.java
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
