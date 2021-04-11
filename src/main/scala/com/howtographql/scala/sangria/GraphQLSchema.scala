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
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks),
      Field(
        "link",
        OptionType(LinkType),
        arguments = List(Argument("id", IntType)),
        resolve = c => c.ctx.dao.getLink(c.arg[Int]("id"))
      ),
      Field(
        "links",
        ListType(LinkType),
        arguments = List(Argument("ids", ListInputType(IntType))),
        resolve = c => c.ctx.dao.getLinks(c.arg[Seq[Int]]("ids"))
      )
    )
  )

  val SchemaDefinition = Schema(QueryType)
}
