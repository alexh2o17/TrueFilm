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

to allow consumers to find top perfoming film (with highest ratio of budget to revenue).
Resulting data are saved in Postgres, so they can be visible through SQL.

## How To Use

To clone and run this application, you'll need [Git](https://git-scm.com), [wget](https://www.gnu.org/software/wget/), [sbt](http://www.scala-sbt.org/) or at least [Docker](https://www.docker.com/) with docker-compose (https://www.docker.com/)  installed on your computer. When you installed these software you can start to process data:

#### 1) Clone repo
Use git to clone this repo. Please remember that all files needs to be in a folder named "truefilm" (case insensitive)
```shell script
# Clone repo
$ git clone git@github.com:alexh2o17/TrueFilm.git
```
#### 1) Start Postgres
Start Postgres Database that will contain data and the tool to make query on Database: PgAmdin

```shell script
# Start database
$ ./script-run.sh startdb
```
#### 2) Download files
Before data processing starts we need to download files
```shell script
# Download_file
$ ./script-run download_files
```
Or download manually and put in ./input folder:
* https://www.dropbox.com/s/0rsg6kag7u3oypx/wiki.xml.gz?dl=1
* https://www.dropbox.com/s/xkcgowtzd0eo9mj/metadata.csv.gz?dl=1


#### 3) Start processing
Start the data process. If you have installed sbt you can simple use
```shell script
# Check if file exists and start processing
$ ./script-run.sh process
```
If you don't have sbt, you can use the docker version of the program. This version in slower than starting with sbt.
```shell script
# Create image if not exist,check if file exist and start processing
$ ./script-run.sh process_docker
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
$ ./script-run.sh test

# Test program with Docker
$ ./script-run.sh test_with_docker
```

Or **stop database** with:
```shell script
# Stop Postgres and PgAmin containers and delete all data
$ ./script-run.sh stopdb
```

## Built with 

- [Scala](https://www.scala-lang.org/) - Scala combines object-oriented and functional programming in one concise, high-level language.
- [FS2](https://fs2.io/#/) - Functional, effectful, concurrent streams for Scala.
- [ZIO](https://zio.dev/) - Type-safe, composable asynchronous and concurrent programming for Scala.
- [doobie](https://tpolecat.github.io/doobie/) - doobie is a pure functional JDBC layer for Scala and Cats.

I choose Scala because I think is one of the best language to interact with Data. I choose ZIO, fs2 and doobie, cause allow to write functional, test and preserve computational resource in a simple way. Moreover, today there few example on git with these tools, so I thought I’d make my own contribution.

## Solution Overview

To complete this challenge I decided to have a streaming approach. All data is in streamed. This type of approach has several advantages, including not loading all the data in memory. This therefore allows to consume very few resources even with a large amount of data.

The two files are processed sequentially. We can therefore divide the processing into three phases:
1) Find Top 1000 Movies with the highest ratio of Budget to Revenue
2) Aggregate information
3) Save Results

In the first step, the **metadata.csv.gz** file containing the movie metadata is processed.
Field taken in consideration are:
* Original Title
* Genres
* Budget
* Revenue
* Production Companies
* Rating
* Release Date

In this phase the first 1000 movies are defined according to the ratio of Budget to Revenue. Before that, the data is cleaned up:
* Movies without one the above field NULL are ignored
* Movies with Budget or Revenue equal to 0 are ignored
* Movies with empty title are ignored
* All title are cleaned removing brackets and text in brackets
* All title are cleaned removing right and left whitespaces

The output of this phase is a list of the 1000 movies with the highest ratio of Budget to Revenue.

In the second step, information about the Wikipedia url and the Wikipedia abstract are added to the 1000 movies with higher ratio. 
Data are read from the **wiki.xml.gz file**. Field taken in consideration for each XML documents are:
* Title
* Url
* Abstract

A join is made on the "title" field to allow the aggregation of the data.
Before the join, the data is cleaned up:
* All title field are cleaned removing brackets and text in brackets
* All title are cleaned removing right and left whitespaces
* All title field are cleaned removing text: "Wikipedia: "

The output of this phase is a list of 1000 movies with ratios with the same information as in phase 1 plus the url and the abstract from wikipedia.
Data model of each film metadata is composed by:
* Original Title
* Genres
* Budget
* Revenue
* Production Companies
* Rating
* Year
* Ratio
* WikiLink
* WikiAbstract

In the third step, Top 1000 Movies are saved on postgres DB in **TOPFILM** table.
