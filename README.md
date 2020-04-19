# EDT

The ``easy database tool`` or short ``EDT`` is a quite simple tool to access any relational database that supports JDBC. It supports running any SQL query. If the query lists the contents of a table or a view the result will be displayed. If there are enough information available (read: if all primary keys are part of the result set) you're also able to edit the values in place or even delete a row.

Some further ideas where implemented like listing all available tables/ views and the columns. Even a simple query generator is included.

## No active development

This project is not actively maintained!

It is published to prevent the sources from being deleted "by accident". And maybe the source code also helps other people with their projects - although the code style is gotten a bit old and no pattern like MVC has been used.

## Setup

The project is quite old and therefore (and I'm really sorry for that) no build tool is used. This project is just a plain eclipse project and even the referenced JAR files need to be downloaded. So, to open and run the project:

1. Clone or download the repository
2. Import the project into eclipse (source encoding is: ISO-8859-1)
3. Download JDOM (Version 1) with all its dependencies and fix the build path of the project
4. Have a look at the ``edt.xml`` to add your databases.

## History

It was created during my time at university (active development in 2008 and 2009 - as far as I can remember) because the company I was working at did not have a "cool" database tool. The idea of the project finally turned into my bachelor thesis but the original tool survived quite a long time until better tools where introduced.
