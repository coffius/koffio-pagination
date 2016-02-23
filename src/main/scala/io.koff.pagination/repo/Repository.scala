package io.koff.pagination.repo

import io.koff.pagination.PageRequest
import io.koff.pagination.domain.{Ware, User}
import scala.concurrent.Future

/**
 * Simple repository for user and production data
 */
class Repository {
  /**
   * Returns users page by page
   * @param page page info
   */
  def getPageOfUsers(page: PageRequest): Future[Seq[User]] = {
    println(s"user page request: $page")

    val offset = page.index * page.count
    if(offset >= 100) {
      Future.successful(Seq.empty)
    } else {
      Future.successful((offset to offset + page.count - 1).map(str => User(s"user#$str")))
    }
  }

  /**
   * Returns user wares page by page
   * @param user user whose wares should be returned
   * @param page page info
   */
  def getPageOfWares(user: User, page: PageRequest):Future[Seq[Ware]] = {
    println(s"ware page request - user: $user, page: $page")

    val offset = page.index * page.count
    if(offset >= 20) {
      Future.successful(Seq.empty)
    } else {
      Future.successful((offset to offset + page.count - 1).map(str => Ware(s"${user.name}'s ware#$str")))
    }
  }
}
