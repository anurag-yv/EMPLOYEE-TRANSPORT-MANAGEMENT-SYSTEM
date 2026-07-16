-- Complete schema recreation script for MySQL
-- This will drop and recreate all tables with proper audit columns
-- Run this if you want to reset the database completely

DROP DATABASE IF EXISTS transport_db;
CREATE DATABASE transport_db;

USE transport_db;

-- Create employees table
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) DEFAULT 'Employee',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_employee_email (email)
);

-- Create admins table
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255) DEFAULT 'ADMIN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_admin_email (email)
);

-- Create routes table
CREATE TABLE routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    pickup_time VARCHAR(255),
    booked_seats INT DEFAULT 0,
    budget DOUBLE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_route_src_dest (source, destination)
);

-- Create alerts table
CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(255),
    recipient VARCHAR(255),
    route_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create bookings table
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT,
    route_id BIGINT,
    number_of_seats INT DEFAULT 1,
    passenger_details VARCHAR(255),
    status VARCHAR(255) DEFAULT 'CONFIRMED',
    booked_at TIMESTAMP,
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_booking_emp (employee_id),
    INDEX idx_booking_route (route_id),
    INDEX idx_booking_status (status),
    INDEX idx_booking_idem (idempotency_key)
);