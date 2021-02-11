# TrueFilm
## Table Of Contents

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Features](#features)
- [How To Use](#how-to-use)
- [Built with](#built-with)
- [Solution Overview](#solution-overview)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Features

The aim of the project is to connect to data source containing:
* **metadata.csv.gz:** metadata about movies, taken from IMDB.com 
* **wiki.xml.gz:** information about movies, taken from WIKIPEDIA

to allow consumers to find top perfoming film (with highest rateo).
Resulting data are saved in Postgres, so they can be visible through SQL.

## How To Use

To clone and run this application, you'll need [Git](https://git-scm.com),[wget](https://www.gnu.org/software/wget/), [sbt](http://www.scala-sbt.org/) or at least [Docker](https://www.docker.com/) with docker-compose (https://www.docker.com/)  installed on your computer. When you installed these software you can start to process data:

#### 1) Start Postgres
Start Postgres Database that will contain data and the tool to make query on Database: PgAmdin

```shell script
# start database
$ ./script-run startdb
```
#### 2) Download files
Before data processing starts we need to download files
```shell script
# Download_file
$ ./script-run downloads_files
```
Or download manually and put in ./input folder:
* https://www.dropbox.com/s/0rsg6kag7u3oypx/wiki.xml.gz?dl=1
* https://www.dropbox.com/s/xkcgowtzd0eo9mj/metadata.csv.gz?dl=1


#### 3) Start processing
Start the data process. If you have installed sbt you can simple use
```shell script
# Check if file exists and start processing
$ ./script-run process
```
If you don't have sbt, you can use the docker version of the program. This version in slower than starting with sbt.
```shell script
# Create image if not exist,check if file exist and start processing
$ ./script-run process_docker
```

#### 4) Navigate on Data
You can analize your results througt PgAdmin on http://localhost:5050.
To access to PgAdmin you have to use the default credentials:
* **PgAdmin:** http://localhost:5050
    * username: pgadmin4@pgadmin.org
    * password: admin
    
And setup the connection with database (only on first access)

Here are the connection details:
* **host:** postgres_container
* **username:** postgres
* **password:** changeme

#### 5) Other
You can also thes this program with:
```shell script
# Test program with sbt
$ ./script-run test

# Test program with Docker
$ ./script-run test_with_docker
```

Or **stop database** with:
```shell script
# Stop Postgres and PgAmin containers and delete all data
$ ./script-run stopdb
```

## Built with 

- [Scala](https://www.scala-lang.org/) - Scala combines object-oriented and functional programming in one concise, high-level language.
- [FS2](https://fs2.io/#/) - Functional, effectful, concurrent streams for Scala.
- [ZIO](https://zio.dev/) - Type-safe, composable asynchronous and concurrent programming for Scala.
- [doobie](https://tpolecat.github.io/doobie/) - doobie is a pure functional JDBC layer for Scala and Cats.

I choose Scala because I think is one of the best language to interact with Data. I choose ZIO, fs2 and doobie, cause allow to write functional, test and preserve computational resource in a simple way. Moreover, today there few example on git with these tools, so I thought I’d make my own contribution

## Solution Overview
