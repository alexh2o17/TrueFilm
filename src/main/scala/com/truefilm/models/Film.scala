package com.truefilm.models

case class Film (title:String,budget: Double,year: Int,revenue: Double,rating: Double, productionCompany: String,wikiLink: Option[String] = None, wikiAbstract: Option[String] = None){
  val ratio: Double = if(budget > 0 && revenue > 0) budget/revenue else 0d
}


