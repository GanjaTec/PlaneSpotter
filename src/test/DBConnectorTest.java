package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteDataSource;
import planespotter.constants.Paths;
import planespotter.model.PyAdapter;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectorTest {
    String DB_NAME = "planeTest.db";
    String DB_URL = "jdbc:sqlite:" + DB_NAME;
    SQLiteDataSource DATABASE;


    @BeforeEach
    void setUp() {
        //PyAdapter.runScript("python-helper/helper/dbBuilder.py", "planeTest.db");
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void getConnectionTest() {
        DB_NAME = "plane.db";
        DB_URL = "jdbc:sqlite:" + DB_NAME;
        SQLiteDataSource DATABASE;
    }

    void injectionTest(){}

    @Test
    void createPreparedStatementTest() {
    }

    @Test
    void queryDBTest() {
    }
}