package io.koff.pagination

import io.koff.pagination.domain.{Ware, User}

import scala.concurrent.Future

/**
 * Simple email service
 */
class EmailService {

  /**
   * Sends an "email" - prints user and ware data to the console
   */
  def sendEmail(user: User, product: Ware): Future[_] = {
    Future.successful {
      println(s"send mail to [${user.name}] about [${product.name}]")
    }
  }
}
