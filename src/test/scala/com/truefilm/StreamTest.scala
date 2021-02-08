package com.truefilm


import java.nio.file.Path

import cats.effect.Blocker
import com.truefilm.configuration.Configuration
import com.truefilm.sqldb.{ClientDB, CustomTransactor}
import zio.test.{DefaultRunnableSpec, suite, testM, _}
import zio.test.Assertion._
import flow._
import zio._
import zio.blocking.Blocking

import scala.concurrent.ExecutionContext.global
import scala.io.Source
object StreamTest extends DefaultRunnableSpec with StreamUtil {
  def test1Url()  = getClass.getResource("/test1.csv.gz")

  val layer = (Configuration.test ++ Blocking.live) >>> CustomTransactor.transactorLive >>> ClientDB.live >>> Stream.live
  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("Testing Stream")(

    testM("Testing complete flow"){
      val blocker = Blocker.liftExecutionContext(global)

      for{
      stream <- readTopFilm(Path.of(test1Url.toURI),blocker,1000,';').provideLayer(layer)
      } yield{
        assert(stream.nonEmpty)(isTrue) &&
        assert(stream.size)(equalTo(2)) &&
        assert(stream.head._1)(equalTo("Toy Story")) &&
        assert(stream.last._1)(equalTo("Jumanji"))
      }
    },
    testM("Testing complete flow with 1 top"){
      val blocker = Blocker.liftExecutionContext(global)
      for{
        stream <- readTopFilm(Path.of(test1Url.toURI),blocker,1,';').provideLayer(layer)
      } yield{
        assert(stream.nonEmpty)(isTrue) &&
        assert(stream.head._1)(equalTo("Jumanji")) &&
        assert(stream.size)(equalTo(1))

      }
    },
    testM("Testing read file as lines"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamFromZippedFile(Path.of(test1Url.toURI),blocker,1024*32).compile.toList
      } yield{
        assert(stream.nonEmpty)(isTrue)
      }
    },
    testM("Testing read file as row"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamToRow(streamFromZippedFile(Path.of(test1Url.toURI),blocker,1024*32),separator = ';').compile.toList
      } yield{
        assert(stream.nonEmpty)(isTrue) &&
        assert(stream.size)(equalTo(2))
      }
    }
  )

}
