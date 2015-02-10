package com.mnachmia31.connectionpool;

import java.sql.Connection;
import org.joda.time.DateTime;

/**
* Class that stores the date times of active database connections that live
* in the connection pool.
* <p>
* Associates a connection from the connection pool with the date time it was created
* and the date time it was leased to a client.
*
* @author      Michael Nachmias <michael.nachmias@gmail.com>
* @version     1.0
* @since       2014-10-24
*/ 
public class TimedConnection {
    private Connection connection;
    private DateTime timeCreated;
    private DateTime timeLeased;

    /**
    * Default constructor.
    * 
    */
    public TimedConnection() {}

    /**
    * Constructor.
    * <p>
    * Populates the connection as well sets the date time it was created
    */
    public TimedConnection(Connection connection) {
	this.connection = connection;
	this.timeCreated = DateTime.now();
    }

    /**
    * Get the connection that lives in the connection pool 
    *
    * @return Connection The connection
    */ 
    public Connection getConnection() {
        return connection;
    }

    /**
    * Gets the date time the connection from the connection pool was created
    *
    * @return DateTime The date time the conncetion from the connection pool was created
    */ 
    public DateTime getTimeCreated() {
        return timeCreated;
    }

    /**
    * Gets the date time the connection from the connection pool was leased to a 
    * client
    *
    * @return DateTime The date time the conncetion from the connection pool was 
    *                  leased to a client
    */ 
    public DateTime getTimeLeased() {
        return timeLeased;
    }

    /**
    * Sets the date time the connection from the connection pool was leased to a 
    * client
    *
    * @param DateTime The date time the conncetion from the connection pool was 
    *                  leased to a client
    */ 
    public void setTimeLeased(DateTime timeLeased) {
        this.timeLeased = timeLeased;
    }
}