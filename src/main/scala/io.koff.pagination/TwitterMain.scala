package io.koff.pagination

import com.twitter.util.Future
import io.koff.pagination.domain.User
import io.koff.pagination.repo.Repository
import com.twitter.concurrent.exp.AsyncStream
import com.twitter.util.{Await, Future}
import io.koff.pagination.utils.TwitterScalaFuture._

/**
 * Example for twitter AsyncStream
 */
object TwitterMain {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val repo = new Repository
  private val emailService = new EmailService

  def main(args: Array[String]) {

    val userStream = asyncPageToStream(repo.getPageOfUsers(_).toTwitter)
      .flatMap{
        user => asyncPageToStream(repo.getPageOfWares(user, _).toTwitter).map((user, _))
      }.takeWhile {
        case (user, _) => user.name != "user#5"
      }.mapF {
        case (user, ware) => emailService.sendEmail(user, ware).toTwitter
      }

    val res = userStream.foreach { value => value }

    Await.ready(res)
  }

  def unfold[T, K](zero: T)(value: (T) => Future[Option[(T, K)]]): AsyncStream[Option[(T, K)]] = {
    val result = value(zero)
    val stream = AsyncStream.fromFuture(result)
    stream.flatMap {
      case Some((t,_)) => stream ++ unfold(t)(value)
      case None => AsyncStream.empty
    }
  }

  def unfold2[T, K](value: T)(func: (T) => Future[Option[(T, K)]]): AsyncStream[K] = {
    val result = func(value)
    val stream = AsyncStream.fromFuture(result)
    stream.flatMap{
      case Some((t,k)) => stream ++ unfold(t)(func)
      case None => AsyncStream.empty
    }.map(_.get._2)
  }

  def asyncPageToPageStream[T](pageFunc: PageRequest => Future[Seq[T]]): AsyncStream[Seq[T]] = {
    unfold2[PageRequest, Seq[T]](PageRequest.FirstPage){ page =>
      val pageData = pageFunc(page)
      pageData.map { data => if (data.isEmpty) { None } else { Some(page.next, data) } }
    }
  }

  def asyncPageToStream[T](pageFunc: PageRequest => Future[Seq[T]]): AsyncStream[T] = {
    asyncPageToPageStream(pageFunc).flatMap(AsyncStream.fromSeq)
  }
}
