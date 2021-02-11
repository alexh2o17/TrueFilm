package com.truefilm

import cats.data.NonEmptyList
import com.truefilm.flow.decoder.FilmDecoder
import zio.ZIO
import zio.test.DefaultRunnableSpec
import zio.test.{DefaultRunnableSpec, suite, testM, _}
import zio.test.Assertion._

object DecoderTest extends DefaultRunnableSpec{

  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("Testing Decoder")(
    testM("Testing read Film") {
      val lsSucc= NonEmptyList[String]("",List("","30000000","", "","","","","Toy Story","","","","[{'name': 'Pixar Animation Studios', 'id': 3}]","","30/10/1995","373554033","","","","","","","7.7",""))
      for{
      x <- ZIO.fromEither(FilmDecoder(lsSucc))
      } yield {
        assert(x.budget)(equalTo(30000000d)) &&
        assert(x.title)(equalTo("Toy Story")) &&
        assert(x.productionCompanies)(equalTo("[{'name': 'Pixar Animation Studios', 'id': 3}]")) &&
        assert(x.year)(equalTo(1995)) &&
        assert(x.revenue)(equalTo(373554033d)) &&
        assert(x.rating)(equalTo(7.7d)) &&
        assert(x.ratio)(equalTo(30000000d/373554033d))
      }
    },
    testM("Testing read Film empty") {
      val lsSucc= NonEmptyList[String]("",List("","","", "","","","","","","","","","","","","","","","","","","",""))
      for{
        x <- ZIO.fromEither(FilmDecoder(lsSucc))
      } yield {
        assert(x.budget)(equalTo(0d)) &&
        assert(x.title)(equalTo("")) &&
        assert(x.productionCompanies)(equalTo("")) &&
        assert(x.year)(equalTo(0)) &&
        assert(x.revenue)(equalTo(0d)) &&
        assert(x.rating)(equalTo(0d)) &&
        assert(x.ratio)(equalTo(0d))
      }
    },
    testM("Testing read Film Invalid") {
      val lsSucc= NonEmptyList[String]("",List("","Error","", "","","","","Toy Story","","","","[{'name': 'Pixar Animation Studios', 'id': 3}]","","30/10/1995","373554033","","","","","","","7.7",""))
      for{
        x <- ZIO.effect(FilmDecoder(lsSucc))
      } yield {
        assert(x.isLeft)(isTrue)
      }
    },
    testM("Testing read Film Too Short") {
      val lsSucc= NonEmptyList[String]("", List(""))
      for{
        x <- ZIO.effect(FilmDecoder(lsSucc))
      } yield {
        assert(x.isLeft)(isTrue)
      }
    },
      )
}
