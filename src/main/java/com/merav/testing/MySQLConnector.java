package com.merav.testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface MySQLConnector {



    Connection openConnection();
    void closeConnection();
}
