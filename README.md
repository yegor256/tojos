# NoSQL Single-File Database

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/tojos)](http://www.rultor.com/p/yegor256/tojos)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/yegor256/tojos/actions/workflows/mvn.yml/badge.svg)](https://github.com/yegor256/tojos/actions/workflows/mvn.yml)
[![PDD status](http://www.0pdd.com/svg?name=yegor256/tojos)](http://www.0pdd.com/p?name=yegor256/tojos)
[![Maintainability](https://api.codeclimate.com/v1/badges/742bde48ea6fabdba1ce/maintainability)](https://codeclimate.com/github/yegor256/tojos/maintainability)
[![Maven Central](https://img.shields.io/maven-central/v/com.yegor256/tojos.svg)](https://maven-badges.herokuapp.com/maven-central/com.yegor256/tojos)
[![Javadoc](http://www.javadoc.io/badge/com.yegor256/tojos.svg)](http://www.javadoc.io/doc/com.yegor256/tojos)
[![codecov](https://codecov.io/gh/yegor256/tojos/branch/master/graph/badge.svg)](https://codecov.io/gh/yegor256/tojos)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/tojos)](https://hitsofcode.com/view/github/yegor256/tojos)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/yegor256/tojos/blob/master/LICENSE.txt)

It's a simple manager of "records" in a text file of CSV, JSON, etc. format.
It's something you would use when you don't want to run a full database, but
just a list of lines in a file is not enough. You need a file with structured
records.

You add this to your `pom.xml`:

```xml
<dependency>
  <groupId>com.yegor256</groupId>
  <artifactId>tojos</artifactId>
  <version>0.18.4</version>
</dependency>
```

Then, to manage `books.csv` file:

```java
import com.yegor256.tojos.MnCsv;
import com.yegor256.tojos.TjDefault;
import com.yegor256.tojos.Tojo;
import com.yegor256.tojos.Tojos;

Tojos tojos = new TjDefault(new MnCsv("books.csv"));
Tojo t1 = tojos.add("Object Thinking"); // unique ID
t1.set("author", "David West");
Tojo t2 = tojos.select(
    t -> t.get("author").equals("David West")
).get(0);
```

Each record has a unique ID, which is also the first column.

## How to Contribute

Fork repository, make changes, send us a [pull request](https://www.yegor256.com/2014/04/15/github-guidelines.html).
We will review your changes and apply them to the `master` branch shortly,
provided they don't violate our quality standards. To avoid frustration,
before sending us your pull request please run full Maven build:

```bash
mvn clean install -Pqulice
```

You will need Maven 3.3+ and Java 8+.

## How Fuzz Testing Works

We use [JQF](https://github.com/rohanpadhye/JQF) for fuzz testing. It helps
us find inputs for some of our tests which are not obvious, but they
still break the code. Here is how you can run it:

```bash
mvn test jqf:fuzz
```

If after this step you see any files in the
`target/fuzz-results/com.yegor256.tojos.Fuzzing/fuzzMnTabs/failures/`
directory, you got a few failures, very good!
Now, you reproduce them in order to understand what's wrong:

```bash
mvn jqf:repro -Dinput=target/fuzz-results/com.yegor256.tojos.Fuzzing/fuzzMnTabs/failures/id_000000
```

You should see a stack trace and a few lines of code that caused the failure.
