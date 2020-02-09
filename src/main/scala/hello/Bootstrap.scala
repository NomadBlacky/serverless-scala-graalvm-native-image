package hello

import com.amazonaws.services.lambda.runtime._
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scalaj.http.{Http, HttpResponse}

import scala.beans.BeanProperty

trait Bootstrap[Request, Response] {
  def objectMapper: ObjectMapper
  def runtime: String
  def requestHandler: RequestHandler[Request, Response]

  def main(args: Array[String]): Unit = {
    while (true) {
      val HttpResponse(body, _, headers) = Http(
        s"http://$runtime/2018-06-01/runtime/invocation/next"
      ).asString
      val requestId = headers("lambda-runtime-aws-request-id").head

      try {
        val request =
          objectMapper.readValue(body, new TypeReference[Request] {})
        val response = requestHandler.handleRequest(request, ???)
        val responseJson = objectMapper.writeValueAsString(response)
        Http(
          s"http://$runtime/2018-06-01/runtime/invocation/$requestId/response"
        ).postData(responseJson).asString
      } catch {
        case e: Exception =>
          Http(
            s"http://$runtime/2018-06-01/runtime/invocation/$requestId/error"
          ).postData(e.getMessage).asString
      }
    }
  }
}

object BootstrapImpl extends Bootstrap[Request, Response] {
  val objectMapper
    : ObjectMapper = new ObjectMapper() //.registerModule(DefaultScalaModule)
  val runtime: String = System.getProperty("AWS_LAMBDA_RUNTIME_API")
  val requestHandler: RequestHandler[Request, Response] = new Handler
}

case class RuntimeApiContext(@BeanProperty awsRequestId: String,
                             @BeanProperty logGroupName: String,
                             @BeanProperty logStreamName: String,
                             @BeanProperty functionName: String,
                             @BeanProperty functionVersion: String,
                             @BeanProperty invokedFunctionArn: String,
                             @BeanProperty identity: CognitoIdentity,
                             @BeanProperty clientContext: ClientContext,
                             @BeanProperty remainingTimeInMillis: Int,
                             @BeanProperty memoryLimitInMB: Int,
                             @BeanProperty logger: LambdaLogger)
    extends Context

object RuntimeApiContext {
  def newContext(headers: Map[String, IndexedSeq[String]]) =
    RuntimeApiContext(
      awsRequestId = headers("Lambda-Runtime-Aws-Request-Id").head,
      logGroupName = ???,
      logStreamName = ???,
      functionName = ???,
      functionVersion = ???,
      invokedFunctionArn = headers("Lambda-Runtime-Invoked-Function-Arn").head,
      identity = ???,
      clientContext = ???,
      remainingTimeInMillis = ???,
      memoryLimitInMB = ???,
      logger = ???
    )
}
