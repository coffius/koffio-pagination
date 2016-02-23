package io.koff.pagination

import io.koff.pagination.domain.User
import io.koff.pagination.repo.Repository
import scala.concurrent.duration._

import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * Example of recursive approach
 */
object RecursiveMain {
  import scala.concurrent.ExecutionContext.Implicits.global
  private val WaitTime  = 10 seconds

  val repo = new Repository
  val emailService = new EmailService

  def main(args: Array[String]) {
    val result = forEachPage()(forUserPage)
    Await.result(result, WaitTime)
  }

  private def forWarePage(user: User, page: PageRequest): Future[Boolean] = {
    for {
      wares <- repo.getPageOfWares(user, page)
      futSeq = wares.map { ware => emailService.sendEmail(user, ware) }
      _ <- Future.sequence(futSeq)
    } yield {
      wares.nonEmpty
    }
  }

  private def forUserPage(page: PageRequest): Future[Boolean] = {
    for{
      users <- repo.getPageOfUsers(page)
      futSeq = users.map(forWarePage(_, page))
      _ <- Future.sequence(futSeq)
    } yield {
      users.nonEmpty
    }
  }

  private def forEachPage(currPage: PageRequest = PageRequest.FirstPage)
                         (func: PageRequest => Future[Boolean])
                         (implicit ctx: ExecutionContext): Future[_] = {
    func(currPage).flatMap {
      case true =>
        forEachPage(currPage.next)(func)
      case false =>
        Future.successful(())
    }
  }
}
