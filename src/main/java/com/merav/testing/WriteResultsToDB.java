package com.merav.testing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WriteResultsToDB {

    private MySQLConnector mySQLConnector = null;
    private Connection connection = null;

    public void writeToMySQL() {

        mySQLConnector = new MySQLConnectorImpl();
        connection = mySQLConnector.openConnection();

        if (connection != null) {
            System.out.println("Here we go");
        } else {
            System.out.println("Failed to make connection!");
        }

        //take all the data from the web and insert in to the table selenium
        //String query = " insert into users (first_name, last_name, date_created, is_admin, num_points)"
        //+ " values (?, ?, ?, ?, ?)";
        String insert = " insert into selenium (title, description, tags, time, language, stars) "+
                " values (?, ?, ? , ?, ?, ?)";
        //
        // String insert = createInsertStatement();
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = connection.prepareStatement(insert);
            preparedStmt.setString(1, "TitleExample");
            preparedStmt.setString(2, "DescriptionExample");
            preparedStmt.setString(3, "Java Ruby");
            preparedStmt.setString(4, "Update on 2018");
            preparedStmt.setString(5, "Java");
            preparedStmt.setString(5, "5.5");
            // execute the preparedstatement
            preparedStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //close connection
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String createInsertStatement(){
        StringBuilder stringBuilder = new StringBuilder();


        return stringBuilder.toString();
    }
}
