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
  def test2Url()  = getClass.getResource("/test2.xml.gz")
  def test3Url()  = getClass.getResource("/test3.xml.gz")

  val layer = (Configuration.test ++ Blocking.live) >>> CustomTransactor.transactorLive >>> ClientDB.test >>> Stream.live
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
    },
    testM("Testing read xml as lines"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamToXMLEvents(streamFromZippedFile(Path.of(test2Url.toURI),blocker,1024*32)).compile.toList
      } yield{
        assert(stream.nonEmpty)(isTrue)
      }
    },
    testM("Testing read xml as filtered events"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamToXMLEvents(streamFromZippedFile(Path.of(test2Url.toURI),blocker,1024*32)).through(filterElements(List("title","url","abstract"))).compile.toList
      } yield{
        assert(stream.nonEmpty)(isTrue)
        assert(stream.size)(equalTo(95))
      }
    },
    testM("Testing read xml as wikiFilm"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamToXMLEvents(streamFromZippedFile(Path.of(test2Url.toURI),blocker,1024*32)).through(filterElements(List("title","url","abstract"))).through(groupElements()).compile.toList
      } yield{
        assert(stream.size)(equalTo(1))
      }
    },
    testM("Testing complete without wiki"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- findAndAggregateTopFilm(Path.of(test2Url.toURI),Path.of(test1Url.toURI),blocker,1000,';',1024*32).provideLayer(layer)
        x = println(stream)
      } yield{
        assert(stream.size)(equalTo(2)) &&
        assert(stream.head.wikiAbstract)(isNone) &&
        assert(stream.head.wikiLink)(isNone) &&
        assert(stream.last.wikiAbstract)(isNone) &&
        assert(stream.last.wikiLink)(isNone)
      }
    },
    testM("Testing complete with toy story wiki "){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- findAndAggregateTopFilm(Path.of(test3Url.toURI),Path.of(test1Url.toURI),blocker,1000,';',1024*32).provideLayer(layer)
      } yield{
        assert(stream.size)(equalTo(2)) &&
          assert(stream.head.wikiAbstract)(isSome) &&
          assert(stream.head.wikiLink)(isSome) &&
          assert(stream.last.wikiAbstract)(isNone) &&
          assert(stream.last.wikiLink)(isNone)
      }
    },
    testM("Testing complete with insert and toy story wiki "){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- findAndAggregateTopFilm(Path.of(test3Url.toURI),Path.of(test1Url.toURI),blocker,1000,';',1024*32).provideLayer(layer)
      } yield{
        assert(stream.size)(equalTo(2)) &&
          assert(stream.head.wikiAbstract)(isSome) &&
          assert(stream.head.wikiLink)(isSome) &&
          assert(stream.last.wikiAbstract)(isNone) &&
          assert(stream.last.wikiLink)(isNone)
      }
    },
  )

}
