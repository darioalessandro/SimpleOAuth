1) Select all rows in the users table

select * from users;

2) print schema

describe schema;

3) use schema

USE authorization;

4) insert

INSERT INTO users(username, password) VALUES ('dario','dario');

5) Alter schema

ALTER KEYSPACE "authentication" WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};

6) create table

CREATE TABLE users ( username varchar, password varchar, PRIMARY KEY (username));

CREATE TABLE tokens ( username varchar,otoken varchar, refreshToken varchar, PRIMARY KEY (otoken));

7) alter table 

ALTER TABLE tokens ADD creation timestamp;



