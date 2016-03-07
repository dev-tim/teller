# --- !Ups
CREATE TABLE CUSTOMER(
  ID BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  REMOTE_ID VARCHAR(32) NOT NULL,
  OBJECT_ID BIGINT NOT NULL,
  OBJECT_TYPE TINYINT(2) NOT NULL DEFAULT 0,
  CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CREATED_BY VARCHAR(254) NOT NULL,
  UPDATED TIMESTAMP NOT NULL,
  UPDATED_BY VARCHAR(254) NOT NULL);

INSERT INTO CUSTOMER(REMOTE_ID, OBJECT_ID, OBJECT_TYPE, CREATED_BY, UPDATED, UPDATED_BY)
  SELECT CUSTOMER_ID, ID, 0, CONCAT_WS(' ', FIRST_NAME, LAST_NAME), CURRENT_TIMESTAMP, CONCAT_WS(' ', FIRST_NAME, LAST_NAME)
    FROM PERSON WHERE CUSTOMER_ID IS NOT NULL;

INSERT INTO CUSTOMER(REMOTE_ID, OBJECT_ID, OBJECT_TYPE, CREATED_BY, UPDATED, UPDATED_BY)
  SELECT CUSTOMER_ID, ID, 1, CREATED_BY, CURRENT_TIMESTAMP, UPDATED_BY
    FROM ORGANISATION WHERE CUSTOMER_ID IS NOT NULL;

ALTER TABLE PERSON DROP COLUMN CUSTOMER_ID;
ALTER TABLE ORGANISATION DROP COLUMN CUSTOMER_ID;

# --- !Downs
ALTER TABLE PERSON ADD COLUMN CUSTOMER_ID VARCHAR(254);
ALTER TABLE ORGANISATION ADD COLUMN CUSTOMER_ID VARCHAR(254);
UPDATE PERSON p, CUSTOMER c SET p.CUSTOMER_ID = c.REMOTE_ID WHERE p.ID = c.OBJECT_ID AND c.OBJECT_TYPE = 0;
UPDATE ORGANISATION o, CUSTOMER c SET o.CUSTOMER_ID = c.REMOTE_ID WHERE o.ID = c.OBJECT_ID AND c.OBJECT_TYPE = 1;
DROP TABLE CUSTOMER;
