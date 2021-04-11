package com.howtographql.scala.sangria

import models._
import sangria.schema._
import sangria.macros.derive._

object GraphQLSchema {
  //val LinkType = ObjectType[Unit, Link](
  //  "Link",
  //  fields[Unit, Link](
  //    Field("id", IntType, resolve = _.value.id),
  //    Field("url", StringType, resolve = _.value.url),
  //    Field("description", StringType, resolve = _.value.description)
  //  )
  //)
  implicit val LinkType = deriveObjectType[Unit, Link]()
  val QueryType = ObjectType(
    "Query",
    fields[MyContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks)
    )
  )
  val SchemaDefinition = Schema(QueryType)
}
