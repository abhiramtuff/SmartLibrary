# Smart Library Management System

A web-based Library Management System developed using Java Object-Oriented Programming, MySQL, HTML, CSS, and JavaScript.

## Overview

The Smart Library Management System is designed to manage books, registered users, and borrowing activities through a simple web-based interface.

The project uses a Java-based backend with Object-Oriented Programming principles, MySQL for persistent data storage, and a frontend built with HTML, CSS, and JavaScript.

## Technologies Used

- Java
- Object-Oriented Programming (OOP)
- MySQL
- JDBC
- HTML
- CSS
- JavaScript

## Main Features

### Book Management
- Add books
- Update book details
- Remove books
- Search books
- Track book availability

### User Management
- Register users
- Search users
- Update user information
- Remove users
- Track borrowed books

### Borrowing Management
- Issue books
- Return books
- Check book availability
- Enforce borrowing limits
- Renew books
- Place holds
- Maintain borrow records
- Track due dates and applicable late fees

### Dashboard
- Total books
- Available books
- Borrowed books
- Total users
- Active borrow records

## OOP Concepts Used

The project demonstrates Object-Oriented Programming concepts including:

- Encapsulation
- Inheritance
- Abstraction
- Classes and Objects

For example, the `Librarian` class inherits from the `User` class.

```java
public class Librarian extends User