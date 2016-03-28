package io.koff.pagination.utils

import com.twitter.util.{Future => TwitterFut, Promise => TwitterProm}

import scala.concurrent.{ExecutionContext, Future => ScalaFut, Promise => ScalaProm}
import scala.util.{Failure, Success, Try}
import com.twitter.{util => twitter}
/**
 * Helper to convert between scala and twitter futures
 */
object TwitterScalaFuture {
  implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
    case Success(r) => twitter.Return(r)
    case Failure(ex) => twitter.Throw(ex)
  }

  implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
    case twitter.Return(r) => Success(r)
    case twitter.Throw(ex) => Failure(ex)
  }

  // Twitter -> Scala
  implicit class TwitterToScala[T](val twitterFut: TwitterFut[T]) extends AnyVal {
    def toScala: ScalaFut[T] = {
      val promise = ScalaProm[T]()
      twitterFut.respond(promise complete _)
      promise.future
    }
  }

  implicit class ScalaToTwitter[T](val scalaFut: ScalaFut[T]) extends AnyVal {
    def toTwitter(implicit execCtx: ExecutionContext): TwitterFut[T] = {
      val promise = TwitterProm[T]()
      scalaFut.onComplete { result => promise.update(result) }
      promise
    }
  }
}
