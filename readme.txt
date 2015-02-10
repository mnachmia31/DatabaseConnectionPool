The Connection Pool


What is it?
------------------------------

The Connection Pool is Java program that creates a pool of active database connections. A pool of database connections are initialized and kept ready for use rather than allocating and destroying them on demand. A client of the connection pool will request a connection from the pool and perform operations on it. When the client has finished, it releases the connection to be returned to the pool rather than destroying it.

It is initialized with a minimum number of database connections. As connections are requested, the connection pool checks to see if there any available connections and returns a connection if there is one. If no connection is available it attemps to create a new database connection and return it to the client if the connection pool has not reached the maximum number of active database connections. The minimum and maximum active database connections in the connection pool are configurable. Please see below to configure these values.

To ensure the Connection Pool is not becoming stale, leased connections can be monitored to verify they are not idle. Please see below for configuring the timeout threshold for determining whether a leased connection is idle.


System Requirements
------------------------------

JDK:
    1.6 or above
Maven:
    3.0.4 or above
Memory:
    No minimum requirement
Disk:
    No minimum requirement
Operating System:
    Windows
Database:
    The Connection Pool is configured to work with MySQL 5.6.21.1. Other databases can be used, but will require changes to the maven build file (pom.xml). Please see below for configuring the Connection Pool to work with other databases.


Installing The Connection Pool
------------------------------

  1) Unpack the archive to C:\connection_pool
  2) Make sure JAVA_HOME is set to the location of your JDK
  3) Run "java -version" to verify that Java is correctly installed
  4) Make sure MAVEN_HOME is set to the location where Maven is installed
  5) Run "mvn -version" to verify that it is correctly installed


Configuring The Connection Pool
------------------------------

The Connection Pool is configured in the file "database.properties" located in C:\connection_pool\src\main\resources
This property file contains 7 database configurations needed for the Connection Pool:

  1) database.jdbc.driverClassName    - the class name of the JDBC driver used to connect to a specific database
  2) database.jdbc.url                - the database URL of the form jdbc:subprotocol:subname
  3) database.user		      - the database user on whose behalf the connection is being made
  4) database.password		      - the database user's password
  5) database.minimum.connections     - the minimum number of connections in the connection pool
  6) database.maximum.connections     - the maximum number of connections in the connection pool
  7) database.connection.timeout      - the timeout threshold for a leased connection in the connection pool

If you choose to use the Connection Pool with another database, you will need to make the following changes:

  1) Modify the class name of the JDBC driver in the "databse.properties" file
  2) Modify the database URL in the "database.properties" file
  3) Modify the database user in the "database.properties" file
  4) Modify the database password in the "database.properties" file
  5) Modify the maven build file (pom.xml) and include a dependency to download the database driver or manually download the database driver and include it in your java classpath

Before running the Connection Pool, please make sure the database is set up and the configurations are correct.


Logging of Connection Pool activity
------------------------------

All Connection Pool activity is logged to C:\connection_pool\logs\connection_pool.log. Please run the Connection Pool tests to view logging activity.


Building The Connection Pool
------------------------------

  1) Run "mvn compile" from C:\connection_pool to build The Connection Pool program
  2) Verify the build is successful


Testing The Connection Pool
------------------------------

Tests have been created that cover the complete Connection Pool program to ensure it functions properly and as expected. Tests include the creating database connections, initialization of the connection pool, retrieving a connection from the connection pool, releasing a connection back to the connection pool, attempting to exceed the maximum the number of active connections allowed in the connection pool, and closing leased connections in the connection pool that are idle.

To run the Connection Pool tests:

  1) Run "mvn test-compile" from C:\connection_pool to build The Connection Pool tests
  2) Verify the build is successful
  3) Run "mvn test" to run the Connection Pool tests
  4) Check the test logs in C:\connection_pool\target\surefire-reports to ensure all tests completed successfully