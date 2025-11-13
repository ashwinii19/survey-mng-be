package com.survey.config;  

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BatchSchemaInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🧱 Checking Spring Batch tables...");

        // Check if main batch table exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'BATCH_JOB_INSTANCE'",
            Integer.class
        );

        if (count == 0) {
            System.out.println("🔧 Creating ALL Spring Batch tables...");
            
            // Complete Spring Batch schema - ALL FIXES APPLIED
            String schemaSql = """
                CREATE TABLE BATCH_JOB_INSTANCE  (
                    JOB_INSTANCE_ID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                    VERSION BIGINT,
                    JOB_NAME VARCHAR(100) NOT NULL,
                    JOB_KEY VARCHAR(32) NOT NULL,
                    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
                ) ENGINE=InnoDB;

                CREATE TABLE BATCH_JOB_EXECUTION  (
                    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                    VERSION BIGINT,
                    JOB_INSTANCE_ID BIGINT NOT NULL,
                    CREATE_TIME DATETIME(6) NOT NULL,
                    START_TIME DATETIME(6) DEFAULT NULL,
                    END_TIME DATETIME(6) DEFAULT NULL,
                    STATUS VARCHAR(10),
                    EXIT_CODE VARCHAR(2500),
                    EXIT_MESSAGE VARCHAR(2500),
                    LAST_UPDATED DATETIME(6),
                    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
                    references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
                ) ENGINE=InnoDB;

                CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
                    JOB_EXECUTION_ID BIGINT NOT NULL,
                    PARAMETER_NAME VARCHAR(100) NOT NULL,
                    PARAMETER_TYPE VARCHAR(100) NOT NULL,
                    PARAMETER_VALUE VARCHAR(2500),
                    IDENTIFYING CHAR(1) NOT NULL,
                    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
                    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
                ) ENGINE=InnoDB;

                CREATE TABLE BATCH_STEP_EXECUTION  (
                    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                    VERSION BIGINT NOT NULL,
                    STEP_NAME VARCHAR(100) NOT NULL,
                    JOB_EXECUTION_ID BIGINT NOT NULL,
                    CREATE_TIME DATETIME(6) NOT NULL,
                    START_TIME DATETIME(6) DEFAULT NULL,
                    END_TIME DATETIME(6) DEFAULT NULL,
                    STATUS VARCHAR(10),
                    COMMIT_COUNT BIGINT,
                    READ_COUNT BIGINT,
                    FILTER_COUNT BIGINT,
                    WRITE_COUNT BIGINT,
                    READ_SKIP_COUNT BIGINT,
                    WRITE_SKIP_COUNT BIGINT,
                    PROCESS_SKIP_COUNT BIGINT,
                    ROLLBACK_COUNT BIGINT,
                    EXIT_CODE VARCHAR(2500),
                    EXIT_MESSAGE VARCHAR(2500),
                    LAST_UPDATED DATETIME(6),
                    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
                    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
                ) ENGINE=InnoDB;

                CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
                    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                    SERIALIZED_CONTEXT TEXT,
                    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
                    references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
                ) ENGINE=InnoDB;

                CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
                    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                    SERIALIZED_CONTEXT TEXT,
                    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
                    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
                ) ENGINE=InnoDB;

                CREATE TABLE BATCH_JOB_SEQ (
                  ID BIGINT NOT NULL,
                  UNIQUE_KEY CHAR(1) NOT NULL,
                  constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
                ) ENGINE=InnoDB;

                CREATE TABLE BATCH_JOB_EXECUTION_SEQ (
                  ID BIGINT NOT NULL,
                  UNIQUE_KEY CHAR(1) NOT NULL,
                  constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
                ) ENGINE=InnoDB;

                CREATE TABLE BATCH_STEP_EXECUTION_SEQ (
                  ID BIGINT NOT NULL,
                  UNIQUE_KEY CHAR(1) NOT NULL,
                  constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
                ) ENGINE=InnoDB;
            """;

            for (String statement : schemaSql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    try {
                        jdbcTemplate.execute(statement);
                    } catch (Exception e) {
                        System.out.println("⚠️ Could not execute: " + statement.substring(0, Math.min(50, statement.length())) + "...");
                    }
                }
            }

            // Initialize sequence tables
            jdbcTemplate.execute("INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) VALUES (0, '0')");
            jdbcTemplate.execute("INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) VALUES (0, '0')");
            jdbcTemplate.execute("INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) VALUES (0, '0')");

            System.out.println("✅ ALL Spring Batch tables created and initialized!");
        } else {
            System.out.println("✅ Spring Batch tables already exist.");
            
            // Fix 1: Make START_TIME nullable in BATCH_STEP_EXECUTION
            try {
                jdbcTemplate.execute("ALTER TABLE BATCH_STEP_EXECUTION MODIFY COLUMN START_TIME DATETIME(6) DEFAULT NULL");
                System.out.println("✅ Fixed START_TIME column to be nullable");
            } catch (Exception e) {
                System.out.println("ℹ️ START_TIME column already nullable or error: " + e.getMessage());
            }
            
            // Fix 2: Check if CREATE_TIME column exists in BATCH_STEP_EXECUTION
            try {
                Integer createTimeExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'BATCH_STEP_EXECUTION' AND column_name = 'CREATE_TIME'",
                    Integer.class
                );
                
                if (createTimeExists == 0) {
                    System.out.println("🔄 Adding missing CREATE_TIME column to BATCH_STEP_EXECUTION...");
                    jdbcTemplate.execute("ALTER TABLE BATCH_STEP_EXECUTION ADD COLUMN CREATE_TIME DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)");
                    System.out.println("✅ CREATE_TIME column added successfully!");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error checking/adding CREATE_TIME column: " + e.getMessage());
            }
            
            // Fix 3: Check if BATCH_JOB_EXECUTION_PARAMS has correct columns
            try {
                Integer correctParams = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'BATCH_JOB_EXECUTION_PARAMS' AND column_name = 'PARAMETER_NAME'",
                    Integer.class
                );
                
                if (correctParams == 0) {
                    System.out.println("🔄 Recreating BATCH_JOB_EXECUTION_PARAMS with correct columns...");
                    jdbcTemplate.execute("DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS");
                    jdbcTemplate.execute("""
                        CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
                            JOB_EXECUTION_ID BIGINT NOT NULL,
                            PARAMETER_NAME VARCHAR(100) NOT NULL,
                            PARAMETER_TYPE VARCHAR(100) NOT NULL,
                            PARAMETER_VALUE VARCHAR(2500),
                            IDENTIFYING CHAR(1) NOT NULL,
                            constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
                            references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
                        ) ENGINE=InnoDB
                    """);
                    System.out.println("✅ BATCH_JOB_EXECUTION_PARAMS recreated with correct columns!");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error checking/recreating BATCH_JOB_EXECUTION_PARAMS: " + e.getMessage());
            }
            
            // Fix 4: Check if sequence tables are initialized
            try {
                Integer initializedCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM BATCH_JOB_SEQ WHERE UNIQUE_KEY = '0'",
                    Integer.class
                );
                if (initializedCount == 0) {
                    System.out.println("🔄 Initializing sequence tables...");
                    jdbcTemplate.execute("INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) VALUES (0, '0')");
                    jdbcTemplate.execute("INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) VALUES (0, '0')");
                    jdbcTemplate.execute("INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) VALUES (0, '0')");
                    System.out.println("✅ Sequence tables initialized!");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error initializing sequence tables: " + e.getMessage());
            }
        }
    }
}