package com.truefilm.sqldb

import cats.effect.Blocker
import com.truefilm.configuration
import com.truefilm.configuration.{Config, DbConfig}
import doobie.Transactor
import zio.blocking.Blocking
import zio.{Has, Managed, Task, ZIO, ZLayer, blocking}
import doobie._
import scala.concurrent.ExecutionContext

object CustomTransactor {

  type DBTransactor = Has[Transactor[Task]]

  def mkTransactor(
                    conf: DbConfig,
                    transactEC: ExecutionContext
                  ): Managed[Throwable, Transactor[Task]] = {

    import _root_.zio.interop.catz._
    {for {
      trx <- ZIO.effect(Transactor.fromDriverManager[Task](
        "org.postgresql.Driver", // driver classname
        conf.url,
        conf.user,
        conf.password,
        Blocker.liftExecutionContext(transactEC)
      ))
    } yield trx
  }.toManaged_
  }

  val transactorLive: ZLayer[Has[Config] with Blocking, Throwable, DBTransactor] =
    ZLayer.fromManaged(for {
      config     <- configuration.load().map(_.dbConfig).toManaged_
      connectEC  <- ZIO.descriptor.map(_.executor.asEC).toManaged_
      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }.toManaged_
      transactor <- mkTransactor(config,  blockingEC)
    } yield transactor)
}
