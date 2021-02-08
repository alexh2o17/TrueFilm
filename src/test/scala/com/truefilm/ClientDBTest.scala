//package com.truefilm
//
//import com.opentable.db.postgres.embedded.EmbeddedPostgres
//import com.truefilm.configuration.{Configuration, DbConfig}
//import com.truefilm.models.Film
//import com.truefilm.sqldb.ClientDB
//import zio.test.Assertion.equalTo
//import zio.test.{DefaultRunnableSpec, suite, testM, _}
//import zio.test.Assertion._
//import sqldb._
//import zio._
//import zio.blocking.Blocking
//object ClientDBTest extends DefaultRunnableSpec{
//
//  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("Testing Configuration")(
//
//    testM("Testing read configuration"){
//      val postgres = EmbeddedPostgres.builder().start()
//      for{
//        postgresConn <- ZIO.effect(postgres.getDatabase("postgres","postgres").getConnection).mapError{x => postgres.close();x}
//        createTable <- ZIO.effect(postgresConn.createStatement()).mapEffect(x => x.execute("CREATE TABLE FILM (TITLE VARCHAR(255));")).mapError{x => postgres.close();x}
//        clientDB <- create(Film("Ciao")).provideCustomLayer((Configuration.custom(dbConfig = DbConfig(s"jdbc:postgresql:world://localhost:${postgres.getPort}/embedded","postgres","postgres")) ++ Blocking.live) >>> CustomTransactor.transactorLive >>> ClientDB.live).mapError{ x => postgres.close();x}
//        close <- ZIO.effect(postgres.close())
//      } yield{
//        assert(clientDB.title)(equalTo("ciao"))
//      }
//    }
//  )
//
//}
