package com.truefilm


import java.nio.file.Paths

import cats.effect.Blocker
import com.truefilm.configuration.Configuration
import com.truefilm.sqldb.{ClientDB, CustomTransactor}
import zio.test.{DefaultRunnableSpec, suite, testM, _}
import zio.test.Assertion._
import flow._
import zio.blocking.Blocking

import scala.concurrent.ExecutionContext.global
object StreamTest extends DefaultRunnableSpec with StreamUtil {
  def test1Url  = Paths.get(getClass.getResource("/test1.csv.gz").getPath)
  def test2Url  = Paths.get(getClass.getResource("/test2.xml.gz").getPath)
  def test3Url  = Paths.get(getClass.getResource("/test3.xml.gz").getPath)

  val layer = (Configuration.test ++ Blocking.live) >>> CustomTransactor.transactorLive >>> ClientDB.test >>> Stream.live
  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("Testing Stream")(

    testM("Testing complete flow"){
      val blocker = Blocker.liftExecutionContext(global)
      for{
      stream <- readTopFilm(test1Url,blocker,1000,';').provideLayer(layer)
      } yield{
        assert(stream.nonEmpty)(isTrue) &&
        assert(stream.size)(equalTo(2)) &&
        assert(stream.head._1)(equalTo("Toy Story".toUpperCase)) &&
        assert(stream.last._1)(equalTo("Jumanji".toUpperCase))
      }
    },
    testM("Testing complete flow with 1 top"){
      val blocker = Blocker.liftExecutionContext(global)
      for{
        stream <- readTopFilm(test1Url,blocker,1,';').provideLayer(layer)
      } yield{
        assert(stream.nonEmpty)(isTrue) &&
        assert(stream.head._1)(equalTo("Jumanji".toUpperCase)) &&
        assert(stream.size)(equalTo(1))

      }
    },
    testM("Testing read file as lines"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamFromZippedFile(test1Url,blocker,1024*32).compile.toList
      } yield{
        assert(stream.nonEmpty)(isTrue)
      }
    },
    testM("Testing read file as row"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
          stream <- streamFromZippedFile(test1Url,blocker,1024*32).through(streamToRow(separator = ';')).compile.toList
      } yield{
        assert(stream.nonEmpty)(isTrue) &&
        assert(stream.size)(equalTo(2))
      }
    },
    testM("Testing read xml as lines"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamFromZippedFile(test2Url,blocker,1024*32).through(streamToXMLEvents).compile.toList
      } yield{
        assert(stream.nonEmpty)(isTrue)
      }
    },
    testM("Testing read xml as filtered events"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamFromZippedFile(test2Url,blocker,1024*32).through(streamToXMLEvents).through(filterElements(List("title","url","abstract"))).compile.toList
      } yield{
        assert(stream.nonEmpty)(isTrue)
        assert(stream.size)(equalTo(95))
      }
    },
    testM("Testing read xml as wikiFilm"){
      val blocker = Blocker.liftExecutionContext(global)
      import zio.interop.catz._
      for{
        stream <- streamFromZippedFile(test2Url,blocker,1024*32).through(streamToXMLEvents).through(filterElements(List("title","url","abstract"))).through(groupElements).compile.toList
      } yield{
        assert(stream.size)(equalTo(1))
      }
    },
    testM("Testing complete without wiki"){
      val blocker = Blocker.liftExecutionContext(global)
      for{
        stream <- findAndAggregateTopFilm(test2Url,test1Url,blocker,1000,';',1024*32).provideLayer(layer)
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
      for{
        stream <- findAndAggregateTopFilm(test3Url,test1Url,blocker,1000,';',1024*32).provideLayer(layer)
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
      for{
        stream <- findAndAggregateTopFilm(test3Url,test1Url,blocker,1000,';',1024*32).provideLayer(layer)
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
