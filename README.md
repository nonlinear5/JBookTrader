[
# JBookTrader

JBookTrader is a Java-based trading platform that connects to Interactive Brokers via the TWS API. It provides tools for algorithmic trading, market data analysis, and order management.

## Prerequisites

- Java 13 or higher
- Maven 3.x


## Setup

1. **Clone the repository:**

   ```sh
   git clone https://github.com/nonlinear5/jbooktrader.git
   cd jbooktrader
   ```

2. **Install the TWS API JAR to your local Maven repository:**

   ```sh
   mvn install:install-file -Dfile=lib/TwsApi-10.37.jar -DgroupId=com.ib -DartifactId=twsapi -Dversion=10.37 -Dpackaging=jar
   ```
This is needed because the TWS API JAR is not available in public Maven repositories.

## Build

To build the project and create a runnable JAR with all dependencies:

```sh
mvn clean package
```

The output JAR will be in the `target` directory, named like:

```
jbooktrader.jar
```

## Run

To start the application:

```sh
java -jar target/JBookTrader.jar
```

## Support
For support and questions, please join the [JBookTrader Google Group
](https://groups.google.com/g/jbooktrader)