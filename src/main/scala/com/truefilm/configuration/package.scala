package com.truefilm

import com.truefilm.configuration.Configuration.Configuration
import zio._

package object configuration {

  def load(): RIO[Configuration, Config] = ZIO.access(_.get)

}
