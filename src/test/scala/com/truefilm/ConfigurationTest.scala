package com.truefilm

import com.truefilm.configuration.Configuration
import zio.test.{DefaultRunnableSpec, suite, testM, _}
import zio.test.Assertion._

object ConfigurationTest  extends DefaultRunnableSpec{

  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("Testing Configuration")(

      testM("Testing read configuration"){
        for{
        config <- configuration.load().map(_.dbConfig).provideLayer(Configuration.test)
        } yield{
          assert(config.url)(equalTo("127.0.0.1"))
          assert(config.user)(equalTo("test"))
          assert(config.password)(equalTo("test"))
        }
      },
        testM("Testing read configuration LIVE"){
        for{
          config <- configuration.load().map(_.dbConfig).provideLayer(Configuration.live)
        } yield{
          assert(config.url)(equalTo("127.0.0.1"))
          assert(config.user)(equalTo("test"))
          assert(config.password)(equalTo("test"))
        }
      }
    )

}
