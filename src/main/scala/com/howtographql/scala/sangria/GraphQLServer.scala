package com.howtographql.scala.sangria

import spray.json.{JsObject, JsString, JsValue}
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import sangria.parser.QueryParser
import scala.util.{Failure, Success}

import sangria.ast.Document
import sangria.execution._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import sangria.marshalling.sprayJson._
//import akka.http.scaladsl.server._

object GraphQLServer {
  private val dao = DBSchema.createDatabase
  def endpoint(requestJSON: JsValue)(implicit ec: ExecutionContext): Route = {
    val JsObject(fields) = requestJSON
    val JsString(query)  = fields("query")
    QueryParser.parse(query) match {
      case Success(queryAst) =>
        val operation = fields.get("operationName") collect { case JsString(op) => op }
        val variables = fields.get("variables") match {
          case Some(obj: JsObject) => obj
          case _                   => JsObject.empty
        }
        complete(executeGraphQLQuery(queryAst, operation, variables))
      case Failure(error) =>
        //complete(BadRequest, JsObject("error" -> JsString(error.getMessage)))
        complete(BadRequest)
    }
  }
  private def executeGraphQLQuery(query: Document, operation: Option[String], vars: JsObject)(
      implicit ec: ExecutionContext
  ) = {
    Executor.execute(
      GraphQLSchema.SchemaDefinition,
      query,
      MyContext(dao),
      variables = vars,
      operationName = operation
    )
  }
}
