package com.truefilm.sqldb

import com.truefilm.models.Film
import com.truefilm.sqldb.CustomTransactor.DBTransactor
import zio._
import doobie.implicits._
import doobie.{Transactor, Update0}

object ClientDB {


  type ClientDB = Has[ClientDB.Service[Any]]
  type ClientImp = ClientDB.Service[Any]

  trait Service[A] {
    def create(film: Film): RIO[A,Film]
  }

  val live: ZLayer[DBTransactor, Throwable, ClientDB] =
    ZLayer.fromService[Transactor[Task],ClientDB.Service[Any]](tnx =>
     new Service[Any]{
       import zio.interop.catz._
       def create(film: Film): Task[Film] =
         SQL
           .create(film)
           .run
           .transact(tnx)
           .foldM(err => Task.fail(err), _ => Task.succeed(film))

       object SQL {

         def create(film: Film): Update0 =
           sql"""INSERT INTO TOP_FILM (title) VALUES (${film.title})""".update

       }
     }

    )


}
