package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def dashboard = Action { Ok(views.html.dashboard()) }
  def tickets = Action { Ok(views.html.tickets()) }
  def ticket = Action { Ok(views.html.ticket()) }
  def notification = Action { Ok(views.html.notification()) }
  def edit = Action { Ok(views.html.edit()) }
  def status = Action { Ok(views.html.status()) }
  def dispatch = Action { Ok(views.html.dispatch()) }
  def statistic = Action { Ok(views.html.statistic()) }
  def build = Action { Ok(views.html.build()) }

}
