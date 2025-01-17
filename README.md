# Database Project - Théta θ

#### Author: Matěj Červenka C4c 17.1.2025
#### School Project: Střední průmyslová škola elektrotechnická, Praha 2, Ječná 30
#### Contact: matej.cervenka1106@gmail.com


## Overview

This project is a database-driven application that manages users, products, categories, and orders for a business. It provides functionalities to manage users, create and update products, assign products to categories, process orders, and maintain an order-product relationship. The application follows the Active Record design pattern for entity management, and the business logic is handled in the controllers. The project uses Spring MVC for backend processing and Thymeleaf as the template engine for rendering HTML views.

## Application
Application is deployed on this ip: [130.162.229.180](http://130.162.229.180/)

### Database Schema

The following database schema is used in the application:

#### 1. **Users Table**

The `user` table manages the information of customers who interact with the system. This table includes essential user data such as name, surname, email, password, and role.

| Column     | Type          | Description                                      |
|------------|---------------|--------------------------------------------------|
| id         | INT           | Primary key, auto-incremented.                   |
| name       | NVARCHAR(100) | User's first name.                               |
| surname    | NVARCHAR(100) | User's surname.                                  |
| password   | NVARCHAR(255) | User's password (hashed for security).          |
| email      | NVARCHAR(100) | User's unique email address.                     |
| role       | NVARCHAR(5)   | User's role (either 'ADMIN' or 'USER').          |
| createdAt  | DATETIME      | Timestamp when the user was created.             |

#### 2. **Products Table**

The `product` table stores information about the products available for purchase. This includes product names, prices, stock levels, and their corresponding category.

| Column     | Type          | Description                                      |
|------------|---------------|--------------------------------------------------|
| id         | INT           | Primary key, auto-incremented.                   |
| name       | NVARCHAR(100) | Name of the product.                             |
| price      | FLOAT         | Price of the product.                            |
| stock      | INT           | Quantity available in stock.                     |
| category_id| INT           | Foreign key to the `category` table.             |

#### 3. **Orders Table**

The `order` table records each order made by customers. It includes the order number, total price, the customer who made the order, and the date of order creation.

| Column     | Type          | Description                                      |
|------------|---------------|--------------------------------------------------|
| id         | INT           | Primary key, auto-incremented.                   |
| customer_id| INT           | Foreign key to the `user` table (the customer). |
| orderNumber| NVARCHAR(50)  | Unique order number.                             |
| orderDate  | DATE          | Date when the order was placed.                  |
| totalPrice | DECIMAL(10,2) | Total price of the order.                        |

#### 4. **OrderProduct Table**

The `orderProduct` table is a many-to-many relationship table between orders and products. It stores the quantity of each product in a given order.

| Column     | Type          | Description                                      |
|------------|---------------|--------------------------------------------------|
| id         | INT           | Primary key, auto-incremented.                   |
| order_id   | INT           | Foreign key to the `order` table.                |
| product_id | INT           | Foreign key to the `product` table.              |
| quantity   | INT           | Quantity of the product in the order.            |

#### 5. **Category Table**

The `category` table stores the product categories. Each product belongs to a specific category, which helps in organizing the products.

| Column     | Type          | Description                                      |
|------------|---------------|--------------------------------------------------|
| id         | INT           | Primary key, auto-incremented.                   |
| name       | NVARCHAR(50)  | Name of the product category.                    |

---

### Active Record Pattern

In this project, the **Active Record** pattern is used for database entities. Each entity corresponds to a table in the database, and the entities themselves manage the database operations (CRUD operations).

Each entity class has methods for finding records, saving records, updating records, and deleting records. They are responsible for encapsulating database operations such as SQL queries, result handling, and connection management.

### Spring MVC Controllers

The controllers are responsible for handling HTTP requests, performing business logic, and returning the corresponding views. The controllers interact with the database through the entity classes, execute operations, and return the results to the views.

### Views with Thymeleaf

Thymeleaf is used as the template engine for rendering HTML pages. It allows embedding dynamic content inside HTML using expressions. For example, product data, user information, and order details are dynamically inserted into the views using Thymeleaf syntax.

### Database Connection

The application uses a `DatabaseConnection` utility class to manage the database connection. The connection is established using JDBC, and SQL queries are executed to interact with the database.

---

## Application Configuration

The application uses the `application.properties` configuration file, which defines the following options:
```
db.url: URL to connect to the database
db.username: Username to connect to the database
db.password: Password to connect to the database
server.port: Port on which the application runs
```

---

## Import and Export Files

The application supports importing and exporting data in CSV format. Users can import products and orders while export allows you to get a specific interesting statistics about e-shop.

### Import
Required fields for import:
- Product name
- Product price
- Product category
- Quantity in stock

### CSV File example:

```
category_name,name,price,stock
Electronics,Smartphone,699.99,50
Electronics,Laptop,999.99,30
Home Appliances,Vacuum Cleaner,199.99,20
Furniture,Table,149.99,10
```

| Category Name      | Product Name    | Price   | Stock |
|--------------------|-----------------|---------|-------|
| Electronics        | Smartphone      | 699.99  | 50    |
| Electronics        | Laptop          | 999.99  | 30    |
| Home Appliances    | Vacuum Cleaner  | 199.99  | 20    |
| Furniture          | Table           | 149.99  | 10    |

### Export

The export format is in CSV format containing all statistical data from the e-shop report.

---

## Potential Errors

The application may encounter various error conditions, such as:
- **Database connection error**: If the application cannot establish a connection to the database, the settings in the `application.properties` file should be checked.
- **Data import error**: If the data is in the wrong format, the application returns an error and the user must edit the file.

---

### Conclusion
This project demonstrates efficient management of products, orders, and users using a database system and a web application. The use of the Active Record design pattern and the Spring MVC framework enables modularity and scalability of the application.
