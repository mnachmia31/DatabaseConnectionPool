package com.mnachmia31.util;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
* Tests the DatabaseProperties functionality.
* <p>
* The DatabaseProperties stores the database properties used to create database connections and 
* configurations of the connection pool. This class will test that the database properties are
* populated successfully.
*
* @author      Michael Nachmias <michael.nachmias@gmail.com>
* @version     1.0
* @since       2014-10-24
*/ 
public class TestDatabaseProperties {

    private DatabaseProperties databaseProperties;
   
    @Before
    public void setUp() throws Exception {        
        databaseProperties = new DatabaseProperties();
    }

    /**
    * Tests the database properties were populated successfully from the property file
    * 
    * @throws Exception
    */ 
    @Test
    public void testInitializeDatabaseProperties() {
	// Verify the database property file exists
	assertNotNull("The Database Properties were not populated. Please check the name of the database property file used.", databaseProperties.getDriverClassName());
	// Verify the database properties were populated successfully by checking the values of each property
	assertEquals("Incorrect database driver returned.", "com.mysql.jdbc.Driver", databaseProperties.getDriverClassName());
	assertEquals("Incorrect database URL returned.", "jdbc:mysql://localhost:3306/opower", databaseProperties.getUrl());
	assertEquals("Incorrect database username returned.", "root", databaseProperties.getUsername());
	assertEquals("Incorrect database password returned.", "P@ssword1234!!", databaseProperties.getPassword());
	assertEquals("Incorrect database minimum active connections returned.", 5, databaseProperties.getMinimumActiveConnections());
	assertEquals("Incorrect database maximum active connections returned.", 20, databaseProperties.getMaximumActiveConnections());
	assertEquals("Incorrect database database connectiom timeout returned.", 5, databaseProperties.getTimeout());
    }
}