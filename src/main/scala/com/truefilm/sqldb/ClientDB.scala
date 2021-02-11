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
           sql"""INSERT INTO topfilm (title, budget, "year", revenue, rating, genres,productioncompanies, wikilink, wikiabstract, ratio) VALUES (${film.title},${film.budget},${film.year},${film.revenue},${film.rating},${film.genres},${film.productionCompanies},${film.wikiLink},${film.wikiAbstract},${film.ratio})""".stripMargin.update

       }
     }
    )

  val test: ZLayer[DBTransactor, Throwable, ClientDB] =
    ZLayer.fromService[Transactor[Task],ClientDB.Service[Any]](tnx =>
      new Service[Any]{
        def create(film: Film): Task[Film] = Task.succeed(film)
      }
    )


}
