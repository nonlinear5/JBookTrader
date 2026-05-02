
# JBookTrader

JBookTrader is a fully automated trading system (ATS) that can trade various types of market securities during the trading day without user monitoring. All aspects of trading, such as obtaining market prices, analyzing price patterns, making trading decisions, placing orders, monitoring order executions, and controlling the risk are automated according to the user preferences. The central idea behind JBookTrader is to completely remove the human element/bias from trading, so that the trading system can systematically and consistently follow a predefined set of rules.

The features include strategy back testing, optimization, market data recording, and real time trading via the Interactive Brokers API.

JBookTrader is written in Java and is intended for software developers. It is not an "off-the-shelf" product that can be installed and run. Instead, JBookTrader provides a framework for developing automated trading systems and requires a certain amount of programming knowledge and experience in Java. If you are not a software developer or if you don't have much experience programming in Java, JBookTrader is probably not for you.

## Prerequisites

- Java 13 or higher
- Maven 3.x
- Interactive Brokers brokerage account (if you plan to trade live)


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


## User Manual
The user manual is available: [`/docs/JBookTrader.UserGuide.docx`](docs/JBookTrader.UserGuide.docx).
However, it's out of date, so I am working on updating it. 

## Support
For support, questions, and suggestions, please join the [JBookTrader Google Group
](https://groups.google.com/g/jbooktrader)
