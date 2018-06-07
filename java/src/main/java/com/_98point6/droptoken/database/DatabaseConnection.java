package com._98point6.droptoken.database;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Singleton
public class DatabaseConnection {
    private static Logger logger;
    private static Connection conn;

    public DatabaseConnection() {
        logger = LoggerFactory.getLogger(DatabaseConnection.class);
        start();
    }

    public Connection getConnection() {
        return conn;
    }

    private void start() {
        try {
            Driver derbyEmbeddedDriver = new EmbeddedDriver();
            DriverManager.registerDriver(derbyEmbeddedDriver);
            Properties props = new Properties(); // databaseConnection properties
            // providing a user name and password is optional in the embedded
            // and derbyclient frameworks


            /* By default, the schema APP will be used when no username is
             * provided.
             * Otherwise, the schema name is the same as the user name (in this
             * case "user1" or USER1.)
             *
             * Note that user authentication is off by default, meaning that any
             * user can connect to your database using any password. To enable
             * authentication, see the Derby Developer's Guide.
             */

            String dbName = "DropToken"; // the name of the database

            /*
             * This databaseConnection specifies create=true in the databaseConnection URL to
             * cause the database to be created when connecting for the first
             * time. To remove the database, remove the directory derbyDB (the
             * same as the database name) and its contents.
             *
             * The directory derbyDB will be created under the directory that
             * the system property derby.system.home points to, or the current
             * directory (user.dir) if derby.system.home is not set.
             */
            conn = DriverManager.getConnection("jdbc:derby:" + dbName
                    + ";create=true", props);
            conn.setAutoCommit(false);


        } catch (SQLException e) {
            logger.error("Database failed to start", e);
        }
    }

    public void stop() {
        try {
            DriverManager.getConnection
                    ("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if (((e.getErrorCode() == 50000) &&
                    ("XJ015".equals(e.getSQLState())))) {
                logger.info("Derby shut down normally", e);
            } else {
                logger.error("Derby did not shut down normally", e);

            }
        }
    }
}
