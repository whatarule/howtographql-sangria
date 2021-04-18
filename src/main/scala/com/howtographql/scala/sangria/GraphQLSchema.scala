package com.howtographql.scala.sangria

import models._
import sangria.schema._
import sangria.macros.derive._

import sangria.execution.deferred.HasId
import sangria.execution.deferred.{Fetcher, DeferredResolver}

import sangria.ast.StringValue
import akka.http.scaladsl.model.DateTime

import sangria.validation.Violation

object GraphQLSchema {

  private def parseDate(s: String): Either[Violation, DateTime] = {
    DateTime.fromIsoDateTimeString(s).toRight(DateTimeCoerceViolation)
  }
  implicit val GraphQLDateTime = ScalarType[DateTime](
    "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case StringValue(dt, _, _) => parseDate(dt)
      case _                     => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = {
      case s: String => parseDate(s)
      case _         => Left(DateTimeCoerceViolation)
    }
  )

  //val LinkType           = ObjectType[Unit, Link](
  //  "Link",
  //  fields[Unit, Link](
  //    Field("id", IntType, resolve = _.value.id),
  //    Field("url", StringType, resolve = _.value.url),
  //    Field("description", StringType, resolve = _.value.description),
  //    Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt)
  //  )
  //)
  implicit val LinkType  = deriveObjectType[Unit, Link](
    ReplaceField("createdAt", Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt))
  )
  implicit val linkHasId = HasId[Link, Int](_.id)

  val linksFetcher = Fetcher((ctx: MyContext, ids: Seq[Int]) => ctx.dao.getLinks(ids))
  val Resolver     = DeferredResolver.fetchers(linksFetcher)

  val id  = Argument("id", IntType)
  val ids = Argument("ids", ListInputType(IntType))

  val QueryType = ObjectType(
    "Query",
    fields[MyContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks),
      Field(
        "link",
        OptionType(LinkType),
        arguments = id :: Nil,
        resolve = c => linksFetcher.deferOpt(c.arg(id))
      ),
      Field(
        "links",
        ListType(LinkType),
        arguments = ids :: Nil,
        resolve = c => linksFetcher.deferSeq(c.arg(ids))
      )
    )
  )

  val SchemaDefinition = Schema(QueryType)
}
