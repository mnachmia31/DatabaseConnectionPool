package com.mnachmia31.connectionpool;

import com.opower.util.DatabaseProperties;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
* Tests the ConnectionPoolManager functionality.
* <p>
* The ConnectionPoolManager manages a pool of active database connections. This class
* wil test the initialization of the connection pool, retrieving a connection from the
* connection pool, releasing a connection back to the connection pool, attempting to
* exceed the maximum the number of active connections allowed in the connection pool,
* and closing leased connections in the connection pool that are idle.
*
* @author      Michael Nachmias <michael.nachmias@gmail.com>
* @version     1.0
* @since       2014-10-24
*/ 
public class TestConnectionPoolManager {

    private ConnectionPoolManager connectionPoolManager;
    private DatabaseProperties databaseProperties;
 
    /**
    * Create the connection pool manager and database properties that will be used
    * to test the connection pool functionality.
    * 
    * @throws Exception
    */   
    @Before
    public void setUp() throws Exception {        
        connectionPoolManager = new ConnectionPoolManager();
	databaseProperties = new DatabaseProperties();
    }


    /**
    * Tests the initialization of the Connection Pool
    * 
    * @throws Exception
    */ 
    @Test
    public void testInitializeConnectionPool() throws Exception {
	// Verify the database properties were populated. If not the connection pool could not be initalized.
	assertNotNull("The Database Properties were not populated. The Collection Pool was not initialized.", databaseProperties.getDriverClassName());
	// Verify the connection pool initialized properly by checking the minimum and maximum active connections in the connection pool
	assertEquals("The Connection Pool has been initalized. Incorrect number of available connections.", 
			databaseProperties.getMinimumActiveConnections(), connectionPoolManager.getNumberOfAvailableConnections());
	assertEquals("A connection has been acquired. Incorrect number of used connections.", 0, connectionPoolManager.getNumberOfLeasedConnections());
    }

    /**
    * Tests leasing and returning connections in the Connection Pool
    * 
    * @throws Exception
    */ 
    @Test
    public void testGetandReleaseConnection() throws Exception {
	// Verify the database properties were populated. If not the connection pool could not be initalized.
	assertNotNull("The Database Properties were not populated. The Collection Pool was not initialized.", databaseProperties.getDriverClassName());
	Connection connection = null;
	// Attempt to lease a connection from the connection pool
	try {
	    connection = connectionPoolManager.getConnection();
	    // Verify the connection was leased successfully by checking the available and leased connection counts in the connection pool
	    assertEquals("A connection has been acquired. Incorrect number of available connections.", databaseProperties.getMinimumActiveConnections()-1, 
			connectionPoolManager.getNumberOfAvailableConnections());
	    assertEquals("A connection has been acquired. Incorrect number of used connections.", 1, connectionPoolManager.getNumberOfLeasedConnections());
	} catch (SQLException se) {
	    se.printStackTrace();
	} finally {
	    if(connection != null) {
		// Attempt to return the connection to the connection pool
		try {
		    connectionPoolManager.releaseConnection(connection);
		    // Verify the connection was returned successfully by checking the available and leased connection counts in the connection pool
	            assertEquals("A connection has been released. Incorrect number of available connections.", databaseProperties.getMinimumActiveConnections(), 
			connectionPoolManager.getNumberOfAvailableConnections());
	    	    assertEquals("A connection has been released. Incorrect number of used connections.", 0, connectionPoolManager.getNumberOfLeasedConnections());
		} catch (SQLException se) {
	    	    se.printStackTrace();
		} 
	    }
        }
    }

    /**
    * Tests trying to exceed the maximum number of connections in the Connection Pool
    * 
    * @throws Exception
    */
    @Test
    public void testMaximumNumberOfConnections() throws Exception {
	// Verify the database properties were populated. If not the connection pool could not be initalized.
	assertNotNull("The Database Properties were not populated. The Collection Pool was not initialized.", databaseProperties.getDriverClassName());
	List<Connection> usedConnections = new ArrayList<Connection>();
	// Attempt to exceed the maximum number of connections by attempting to lease double the maximum amount allowed
	try {
	    for(int i = 0; i < databaseProperties.getMaximumActiveConnections()*2; i++) {
		Connection connection = connectionPoolManager.getConnection();
	        if (i >= databaseProperties.getMaximumActiveConnections()) {
		    // Verify a null connection is returned when attempting to lease a connection when the maximum threshold has been reached
		    assertEquals("All connections have been acquired. Attempting to get a connection that exceeds the maximum number of connections should return null.", 
                        null, connection);   
		} else {
		    usedConnections.add(connection);
		}
	    }
	    // Verify all of the connections in the connection pool have been leased
	    assertEquals("All connections have been acquired. Incorrect number of available connections.", 0, connectionPoolManager.getNumberOfAvailableConnections());
	    assertEquals("All connections have been acquired. Incorrect number of used connections.", databaseProperties.getMaximumActiveConnections(), 
                        connectionPoolManager.getNumberOfLeasedConnections());
	    
	} catch (SQLException se) {
	    se.printStackTrace();
	} finally {
	    try {
		// Return all of the leased connections to the connection pool
	        for(Connection connection : usedConnections) {
	            connectionPoolManager.releaseConnection(connection);
	    	}
		// Verify all of the connections in the connection pool have been returned
	        assertEquals("All connections have been released. Incorrect number of available connections.", databaseProperties.getMaximumActiveConnections(), 
                    connectionPoolManager.getNumberOfAvailableConnections());
	    	assertEquals("All connections have been released. Incorrect number of used connections.", 0, connectionPoolManager.getNumberOfLeasedConnections());
            } catch (SQLException se) {
	        se.printStackTrace();
            } 
        }
    }

    /**
    * Tests closing idle connections in the Connection Pool
    * 
    * @throws Exception
    */
    @Test
    public void testCloseIdleConnections() throws Exception {
	// Verify the database properties were populated. If not the connection pool could not be initalized
	assertNotNull("The Database Properties were not populated. The Collection Pool was not initialized.", databaseProperties.getDriverClassName());	
	// Get a connection from the connection pool
	Connection connection = connectionPoolManager.getConnection();
	// Verify the connection was leased successfully by checking the available and leased connection counts in the connection pool
	assertEquals("A connection has been acquired. Incorrect number of available connections.", databaseProperties.getMinimumActiveConnections()-1, 
                    connectionPoolManager.getNumberOfAvailableConnections());
	assertEquals("A connection has been acquired. Incorrect number of used connections.", 1, connectionPoolManager.getNumberOfLeasedConnections());
	// Allow the connection to be leased for longer than the timeout threshold so it is considered idle 
    	try {
   	    TimeUnit.SECONDS.sleep(databaseProperties.getTimeout()*2);
	} catch (InterruptedException e) {
    	    e.printStackTrace();
	}
	// Close leased connections in the connection pool that are idle
	int idleConnectionCount = connectionPoolManager.closeIdleConnections();
	// Verify all of the idle connections in the connection pool have been closed
	assertEquals("Idle leased connection has been closed. Incorrect number of available connections.", databaseProperties.getMinimumActiveConnections()-idleConnectionCount, connectionPoolManager.getNumberOfAvailableConnections());
	assertEquals("Idle leased connection has been closed. Incorrect number of used connections.", 0, connectionPoolManager.getNumberOfLeasedConnections());
    }
}