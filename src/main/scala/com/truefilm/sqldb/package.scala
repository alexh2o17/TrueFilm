package com.truefilm

import com.truefilm.models.Film
import com.truefilm.sqldb.ClientDB.ClientDB
import zio._

package object sqldb extends ClientDB.Service[ClientDB]{

  def create(film: Film) : RIO[ClientDB,Film] = RIO.accessM(_.get.create(film))

}
