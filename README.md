<img src="https://www.yegor256.com/images/books/elegant-objects/cactus.svg" height="100px" />

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/cqfn/eo)](http://www.rultor.com/p/cqfn/eo)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/cqfn/eo/actions/workflows/mvn.yml/badge.svg)](https://github.com/cqfn/eo/actions/workflows/mvn.yml)
[![PDD status](http://www.0pdd.com/svg?name=cqfn/eo)](http://www.0pdd.com/p?name=cqfn/eo)
[![Maintainability](https://api.codeclimate.com/v1/badges/e4f7ed144919f7f0d58c/maintainability)](https://codeclimate.com/github/cqfn/eo/maintainability)
[![Maven Central](https://img.shields.io/maven-central/v/com.yegor256/eo-parent.svg)](https://maven-badges.herokuapp.com/maven-central/com.yegor256/eo-parent)

[![codecov](https://codecov.io/gh/cqfn/eo/branch/master/graph/badge.svg)](https://codecov.io/gh/cqfn/eo)
[![Hits-of-Code](https://hitsofcode.com/github/cqfn/eo)](https://hitsofcode.com/view/github/cqfn/eo)
![Lines of code](https://img.shields.io/tokei/lines/github/cqfn/eo)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/cqfn/eo/blob/master/LICENSE.txt)

It's a simple manager of "records" in a text file of CSV, JSON, etc. format.
It's something you would use when you don't want to run a full database, but
just a list of lines in a file is not enough. You need a file with structured
records.

You add this to your `pom.xml`:

```xml
<dependency>
  <groupId>com.yegor256</groupId>
  <artifactId>tojos</artifactId>
</dependency>
```

Then, to manage `books.csv` file:

```java
import com.yegor256.tojos.Tojos;
import com.yegor256.tojos.MonoTojos;

Tojos tojos = new MonoTojos(new CSV("books.csv"))
Tojo t1 = tojos.add("Object Thinking");
t1.set("author", "David West");
Tojo t2 = tojos.select(
  t -> t.get("author").equals("Elegant Objects")
).get(0);
```

## How to Contribute

Fork repository, make changes, send us a [pull request](https://www.yegor256.com/2014/04/15/github-guidelines.html).
We will review your changes and apply them to the `master` branch shortly,
provided they don't violate our quality standards. To avoid frustration,
before sending us your pull request please run full Maven build:

```bash
$ ./mvnw clean install -Pqulice
```

You will need Maven 3.3+ and Java 8+.
