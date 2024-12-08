-- Create the database
CREATE DATABASE login;

-- Select the newly created database for use
USE login;

-- Display an error because 'login' is a database, not a table
-- SELECT * FROM login; 

-- Instead, you should specify a table to select from after creating it.

-- Create the 'roles' table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Create the 'users' table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

select * from users;