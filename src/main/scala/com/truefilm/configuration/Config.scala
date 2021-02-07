package com.truefilm.configuration


final case class Config(dbConfig: DbConfig)

final case class DbConfig(url: String, user: String, password: String)
