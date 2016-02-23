package io.koff.pagination

import io.koff.pagination.domain.User
import io.koff.pagination.repo.Repository
import scala.concurrent.duration._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

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

  /**
   * Execute this function for each page of user wares
   */
  private def forWarePage(user: User, page: PageRequest): Future[Boolean] = {
    for {
      //get page of user wares
      wares <- repo.getPageOfWares(user, page)
      //send email
      futSeq = wares.map { ware => emailService.sendEmail(user, ware) }
      _ <- Future.sequence(futSeq)
    } yield {
      wares.nonEmpty
    }
  }

  /**
   * Execute this function for each page of users
   */
  private def forUserPage(page: PageRequest): Future[Boolean] = {
    for{
      //get a page of users
      users <- repo.getPageOfUsers(page)
      //traverse though user wares for each user
      futSeq = users.map(user => forEachPage()(forWarePage(user, _)))
      _ <- Future.sequence(futSeq)
    } yield {
      users.nonEmpty
    }
  }

  /**
   * Recursive function for traverse through a collection page by page using `func`
   * @param currPage current page
   * @param func function which is called on each step of recursion
   * @param ctx execution context
   * @return result future
   */
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
