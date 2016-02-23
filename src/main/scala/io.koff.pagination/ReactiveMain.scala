package io.koff.pagination

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import io.koff.pagination.repo.Repository

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
 * Pagination using akka-streams
 */
object ReactiveMain {
  import scala.concurrent.ExecutionContext.Implicits.global
  private val WaitTime  = 10 seconds
  private val repo = new Repository
  private val emailService = new EmailService 
  implicit val system = ActorSystem("reactive-pagination")
  implicit val materializer = ActorMaterializer()
  def main(args: Array[String]): Unit = {
    //Define the computation stream
    val source =
      //get users page by page
      asyncPageToSource(repo.getPageOfUsers)
      .flatMapConcat {
        //for each user get wares page by page
        user => asyncPageToSource(repo.getPageOfWares(user, _)).map((user, _))
      }.takeWhile{
        //it is possible to check errors here to stop processing after the first error
        case (user, _) => user.name != "user#5"
      }.mapAsync(1){
        //send email for each (user, ware) pair
        (emailService.sendEmail _).tupled
      }

    //Execute computations
    //Because all computations are executed inside the stream
    //We don't need to do anything here
    val result = source.runForeach(value => value)
    //Wait the result
    Await.result(result, WaitTime)
    //Shutdown the actor system
    system.terminate()
  }

  /**
   * Converts page function to Source of pages
   * @param pageFunc function which receives PageRequest as a parameter and returns an async result
   * @return [[Source]] of pages
   */
  private def asyncPageToPageSource[T](pageFunc: PageRequest => Future[Seq[T]]): Source[Seq[T], Unit] = {
    Source.unfoldAsync(PageRequest.FirstPage){ page =>
      val pageData = pageFunc(page)
      pageData.map(data => if (data.isEmpty){ None } else { Some(page.next, data) })
    }
  }

  /**
   * Converts page function to Source of elements of T
   * @param pageFunc function which receives PageRequest as a parameter and returns an async result
   * @return [[Source]] of pages
   */
  private def asyncPageToSource[T](pageFunc: PageRequest => Future[Seq[T]]): Source[T, Unit] = {
    asyncPageToPageSource(pageFunc).mapConcat{ seq => seq.toList }
  }
}
