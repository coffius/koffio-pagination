package io.koff.pagination

/**
 * Information about a page
 */
case class PageRequest(index: Long, count: Long){
  def next: PageRequest = {
    copy(index = index + 1)
  }
}

object PageRequest {
  val FirstPage = PageRequest(0, 10)
}
