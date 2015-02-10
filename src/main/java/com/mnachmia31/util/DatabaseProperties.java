package com.mnachmia31.util;

import java.io.InputStream;
import java.util.Properties;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.apache.log4j.Logger;

/**
* Class that stores database configuration properties.
* <p>
* The database configuration properties encapsulated in this object are
* the class name of the JDBC driver used to make a database connection,
* the database URL of the form jdbc:subprotocol:subname, the database user 
* on whose behalf the connection is being made, the user's password, the
* minimum number of connections needed to instantiate a collection pool of
* connections to the database, and the maximum number of connections allowed
* in a collection pool of connections to the database.
*
* @author      Michael Nachmias <michael.nachmias@gmail.com>
* @version     1.0
* @since       2014-10-24
* @see DatabaseProperties
*/ 
public class DatabaseProperties {
    // Define a static logger variable so that it references the
    // Logger instance named "ConnectionPoolManager"
    private static Logger logger = Logger.getLogger(DatabaseProperties.class);

    // Name of the database property file that stores the database configurations
    private static final String databasePropertyFileName = "database.properties";

    private String driverClassName;		// Class Name of JDBC driver used to make the database connection
    private String url;				// Database URL				
    private String username;			// The database user on whose behalf the connection is being made
    private String password;			// The database user's password
    private int minimumActiveConnections;	// The minimum number of connections in the collection pool
    private int maximumActiveConnections;	// The maximum number of connections in the collection pool
    private int timeout;			// The timeout threshold to determine if a leased connection in the connection pool is idle

    /**
    * Default constructor.
    * <p>
    * The constructor makes a call to initialize the DatabaseProperties object.
    */
    public DatabaseProperties() {
        initializeDatabaseProperties();
    }

    /**
    * Initializes the DatabaseProperties object by populating it with the database configuration properties read 
    * from the database property file.
    * <p>
    * Initialization occurs when the DatabaseProperties object is created. 
    */ 
    private void initializeDatabaseProperties() {
	// Create an empty property list that will store the database configuration properties
        Properties prop = new Properties();
	// Create an input byte stream to read the data from the database property file
	InputStream input = null;

	try {
	    input = getClass().getClassLoader().getResourceAsStream(databasePropertyFileName);

	    // Check to see if the input byte stream could be not be created from the database property file
	    if (input == null) {
	        throw new FileNotFoundException("Database Property File " + databasePropertyFileName + " not found in the classpath.");
            }

	    // Read the database properties from the input byte stream and store them as key and value pairs in the property list
	    prop.load(input);

	    // Check to see if the Minimum Connections, Maximum Connections, and Timeout threshold values in the Database Property file are valid
	    // The Minimum Active Connection value has to be greater than 0
	    // The Maximum Active Connection value has to be greater than or equal to the Minimum Active Connection value
	    // The Timeout threshold has to be greater than 0
	    if (Integer.parseInt(prop.getProperty("database.minimum.connections")) <= 0) {
		throw new IllegalArgumentException("The Minimum Active Connection value has to be greater than 0.");
	    } else if (Integer.parseInt(prop.getProperty("database.minimum.connections")) > Integer.parseInt(prop.getProperty("database.maximum.connections"))) {
		throw new IllegalArgumentException("The Maximum Active Connection value has to be greater than or equal to the Minimum Active Connection value.");
	    } else if (Integer.parseInt(prop.getProperty("database.connection.timeout")) <= 0) {
		throw new IllegalArgumentException("The Timeout threshold value has to be greater than 0.");
	    }
 
	    // Set the database properties using the property list
	    driverClassName = prop.getProperty("database.jdbc.driverClassName");
	    url = prop.getProperty("database.jdbc.url");
	    username = prop.getProperty("database.username");
	    password = prop.getProperty("database.password");
	    minimumActiveConnections = Integer.parseInt(prop.getProperty("database.minimum.connections"));
	    maximumActiveConnections = Integer.parseInt(prop.getProperty("database.maximum.connections"));
	    timeout = Integer.parseInt(prop.getProperty("database.connection.timeout"));
        } catch (IOException ioe) {
    	    logger.error("I/O error occurred reading the Database Property file " + databasePropertyFileName + ".");
        } catch (IllegalArgumentException iae) {
    	    logger.error("Incorrect Timeout, Minimum Connections, and/or Maximum Connections values in the Database Property file.");
        } finally {
	    // Close the input stream to release any system resources associated with it
            if(input != null) {
                try {
		    input.close();
		} catch (IOException ioe) {
		    logger.error("I/O error occurred closing the Database Property file " + databasePropertyFileName + ".");
		}
            }
        }
    }

    /**
    * Gets the class Name of JDBC driver used to make a database connection
    *
    * @return String Class Name of JDBC driver used to make the database connection
    */ 
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
    * Gets the database URL used to make a database connection
    *
    * @return String Database URL of the form jdbc:subprotocol:subname
    */ 
    public String getUrl() {
        return url;
    }

    /**
    * Gets the database user on whose behalf the connection is being made
    *
    * @return String Database username
    */ 
    public String getUsername() {
        return username;
    }

    /**
    * Gets the the database user's password
    *
    * @return String Database user's password
    */ 
    public String getPassword() {
        return password;
    }

    /**
    * Gets the minimum number of connections needed to instantiate a collection pool 
    * of connections to the database
    *
    * @return int Minimum number of connections needed for a collection pool of connections
    *             to the database
    */ 
    public int getMinimumActiveConnections() {
        return minimumActiveConnections;
    }

    /**
    * Gets the maximum number of connections allowed in a collection pool of connections
    * to the database
    *
    * @return int Maximum number of connections allowed in a collection pool of connections
    *             to the database
    */ 
    public int getMaximumActiveConnections() {
        return maximumActiveConnections;
    }

    /**
    * Gets the timeout threshold for leased connections in the connection pool
    *
    * @return int Timeout threshold for leased connections in the connection pool
    */ 
    public int getTimeout() {
        return timeout;
    }
}