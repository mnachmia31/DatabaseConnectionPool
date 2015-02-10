package com.mnachmia31.connectionpool;

import com.mnachmia31.util.DatabaseProperties;
import com.mnachmia31.util.DateTimeUtil;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.apache.log4j.Logger;

/**
* Class that manages a pool of active database connections.
* <p>
* Designed using the object pool creational design pattern. A pool of database
* connections are initialized and kept ready for use rather than allocating and 
* destroying them on demand. A client of the connection pool will request a 
* connection from the pool and perform operations on it. When the client has 
* finished, it releases the connection to be returned to the pool rather than 
* destroying it.
* <p>
* The connection pool is initialized with a minimum number of database
* connections. As connections are requested, the connection pool checks to see
* if there any available connections and returns a connection if there is one.
* If no connection is available the ConnectionPoolManager attemps to create a
* new database connection and return it to the client if the connection pool
* has not reached the maximum number of active database connections. The
* minimum and maximum number of active database connections are configured in
* the DatabaseProperties class.
* <p>
* The ConnectionPoolManager also contains a method to ensure leased connections 
* are not idle. This method can be run by a Connection Pool Monitor in a separate 
* thread to keep the connection pool from becoming stale.
*
* @author      Michael Nachmias <michael.nachmias@gmail.com>
* @version     1.0
* @since       2014-10-24
* @see DatabaseProperties
* @see ConnectionPoolMonitor
*/ 
public class ConnectionPoolManager implements ConnectionPool {
    // Define a static logger variable so that it references the
    // Logger instance named "ConnectionPoolManager"
    private static Logger logger = Logger.getLogger(ConnectionPoolManager.class);
  
    // List of available connections in the connection pool
    private List<TimedConnection> availableConnections = new ArrayList<TimedConnection>();
    // List of used connections in the connection pool
    private List<TimedConnection> leasedConnections = new ArrayList<TimedConnection>();

    // Object that stores the database connection properties
    private DatabaseProperties databaseProperties;
 
    /**
    * Default constructor.
    * <p>
    * Populates a DatabaseProperties object used to create database connections 
    * and then makes a call to initializes the connection pool.
    */
    public ConnectionPoolManager() {
	// Create DatabaseProperties object that will be used to create the
	// database connections for the connection pool
	databaseProperties = new DatabaseProperties();
	// Initialize the connection pool with the minimum number of active
	// database connections that can be requested
	initializeConnectionPool();
    }

    /**
    * Initializes the ConnectionPoolManager by creating the minimum number
    * number of active database connections and adding them to list of
    * available connections in the connection pool. 
    * <p>
    * Initialization occurs when the connection pool is created. 
    */ 
    private void initializeConnectionPool() {
	// Create the minimum number of active database connections and add them
	// to the list of available connections in the connection pool
        for(int i = 0; i < databaseProperties.getMinimumActiveConnections(); i++) {
	    TimedConnection timedConnection = createConnectionForPool();
	    if (timedConnection != null) {
                availableConnections.add(timedConnection);
	        logger.info("Successfully created a database connection and added it to the Connection Pool.");
	    }
        }
	// Check whether the correct number of connections were created
	if (getNumberOfAvailableConnections() == databaseProperties.getMinimumActiveConnections() && databaseProperties.getMinimumActiveConnections() > 0) {
	    logger.info("Successfully initialized Connection Pool with " + getNumberOfAvailableConnections() + " available database connections.");
	} else {
	    logger.error("Failed to initialize Connection Pool.");
	}
    }

 
    /**
    * Creates the database connections that will be added to the connection pool
    * <p>
    * Database connections are created when the connection pool is initialized as well
    * as when a client requests a connection and there are no available connections
    * in the connection pool (assuming the connection pool does not contain the maximum 
    * number of active database connections).
    *
    * @return TimedConnection The database connection that is created and added to the connection pool; null
    *                    if there was an exception creating the database connection.
    */ 
    private TimedConnection createConnectionForPool() {
        try {
	    // Load the JDBC driver and catch the exception if the database driver class does not exist
            Class.forName(databaseProperties.getDriverClassName());
	    // Create the database connection and catch the exception if a database access error occurs
            Connection connection = (Connection) DriverManager.getConnection(
                databaseProperties.getUrl(), databaseProperties.getUsername(), databaseProperties.getPassword());
	    // Return the connection to be added to the connection pool
	    return new TimedConnection(connection);
        } catch (ClassNotFoundException cne) {
            logger.error("Error creating connection to the database. Database driver class is not valid. Please check the database.properties file and verify the database.jdbc.driverClassName property.");
        } catch (SQLException se) {
            logger.error("Error creating connection to the database. Please check the database.properties file and verify url, username, and password properties are valid.");
        }

        return null;
    }

    /**
    * Checks if the connection pool is full by containing the maximum number of active database
    * connections.
    * <p>
    * The connection pool is full if the number of active database connections (the total from the
    * available connections list and used connections list) is equal to the maximum number of
    * active database connections allowed in the connection pool. The maximum number of active 
    * database connections is configured in the DatabaseProperties class.
    *
    * @return boolean True if the connection pool contains the maximum number of active database
    *                 connections; false otherwise.
    */ 
    private synchronized boolean isConnectionPoolFull() {
	// Check to see if the connection pool is full by comparing the number of active database
	// connections (the total from the available connections list and used connections list) is 
        // equal to the maximum number of active database connections allowed in the connection pool
        if ((availableConnections.size() + leasedConnections.size()) < databaseProperties.getMaximumActiveConnections()) {
            return false;
        }

        return true;
    }

    /**
    * Retrive an available database connection from the connection pool
    * <p>
    * As database connections are requested from a client, the connection pool will 
    * check to see if there any available connections and return a connection if there
    * is one. If no connection is available and the connection pool has not reached
    * the maximum number of active database connections, a new connection is created and
    * returned to the client. The database connection that is returned to the client is
    * moved from the available connections list to the used connections list in the
    * connection pool.
    *
    * @return TimedConnection An available database connection from the connection pool; null if
    *                    no connections in the connection pool are available and the connection
    *                    pool has reached its maximum number of active database connections.
    * @throws SQLException
    */  
    public synchronized Connection getConnection() throws SQLException {
        Connection connection = null;
	TimedConnection timedConnection;

	// Check to see if there are any available connections in the connection pool
        if (availableConnections.size() > 0) {
            timedConnection = availableConnections.get(0);
	    // Set the latest access time the connection was provided to a client
	    timedConnection.setTimeLeased(DateTime.now());
	    leasedConnections.add(timedConnection);
	    connection = timedConnection.getConnection();
            availableConnections.remove(0);    
	    logger.info("Successfully provided an available connection from the Connection Pool to the client.");	
        } else if (!isConnectionPoolFull()) {
	    // Connection pool is not full so create a new connection and provide it to the client
	    timedConnection = createConnectionForPool();
	    if (timedConnection != null) {
		// Set the latest access time the connection was provided to a client
	        timedConnection.setTimeLeased(DateTime.now());
	        leasedConnections.add(timedConnection);
		connection = timedConnection.getConnection();
	        logger.info("Successfully created a database connection and added it to the Connection Pool.");
	        logger.info("Successfully provided an available connection from the Connection Pool to the client.");
	    } else {
	        logger.error("Unable to provide a connection to the client. Error creating a new connection.");
	    }	
        } else {
	    logger.info("Unable to provide a connection to the client. No available connections in the connection pool.");
	}

        return connection;
    }

    /**
    * Releases a used database connection to the connection pool
    * <p>
    * As database connections are released from a client, the connection pool will 
    * move the database connection from the used connections list to the available 
    * connections list in the connection pool.
    *
    * @param Connection The connection that is being released from the connection pool
    * @throws SQLException
    */ 
    public synchronized void releaseConnection(Connection connection) throws SQLException {
	TimedConnection timedConnection = null;

	// Find the TimedConnection in the used connections list associated with the connection 
	// being released by the client
	for(TimedConnection tc : leasedConnections) {
	    if(tc.getConnection() == connection) {
		timedConnection = tc;
		break;
	    }
	}

	// Move the connection being released from the client back to the available connections list
	// in the connection pool
	if(timedConnection != null) {
	    availableConnections.add(timedConnection);
	    leasedConnections.remove(timedConnection);
	    logger.info("Successfully released the connection back to the Connection Pool.");
        } else {
            logger.error("Error releasing the connection back to the Connection Pool.");
	}
    }

    /**
    * Closes leased connections from the connection pool that are idle
    * <p>
    * Need to ensure connections that are leased and being used by the client are not in an idle state. If
    * a connection is idle it needs to be closed. A leased connection is considered idle if it has been
    * leased longer than the timeout threshold.
    *
    * @return int The number of leased connections that are idle and were removed from the connection pool.
    * @throws SQLException
    */ 
    public synchronized int closeIdleConnections() throws SQLException {
	List<TimedConnection> idleConnections = new ArrayList<TimedConnection>();

	// Iterate over the leased connections to determine if any are idle
	for (TimedConnection leasedConnection : leasedConnections) {
	    // Get the amount of time in seconds each connection has been leased for
	    int leasedTime = DateTimeUtil.getTimeDiff(leasedConnection.getTimeLeased());
	    // If the leased time is greater than the timeout threshold the leased connection needs to be closed
	    if(leasedTime > databaseProperties.getTimeout()) {
	        idleConnections.add(leasedConnection);
	    }
	}

	// Remove any idle connections from the connection pool
	for (TimedConnection idleConnection : idleConnections) {
	    // Remove the idle connection from the connection pool
	    leasedConnections.remove(idleConnection);
	    logger.info("Removed idle connection from the Connection Pool.");
	    // Close the idle connection
	    idleConnection.getConnection().close();
	}

	return idleConnections.size();
    }

    /**
    * Gets the number of available database connections from the connection pool
    *
    * @return int The number of available database connections from the connection pool
    */ 
    public synchronized int getNumberOfAvailableConnections() {
        return availableConnections.size();
    }

    /**
    * Gets the number of used database connections from the connection pool
    *
    * @return int The number of used database connections from the connection pool
    */ 
    public synchronized int getNumberOfLeasedConnections() {
        return leasedConnections.size();
    }
}





