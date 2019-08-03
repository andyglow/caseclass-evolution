package caseclass.macros

import scala.reflect.macros.whitebox


private[macros] class Log(c: whitebox.Context) {
  import Log._

  def inf(x: String): Unit = {
    c.info(c.enclosingPosition, x, force = true)
  }

  def fail(x: String): Nothing = {
    c.abort(c.enclosingPosition, x)
  }

  def warn(x: String): Unit = {
    c.warning(c.enclosingPosition, x)
  }

  def err(x: String): Unit = {
    c.error(c.enclosingPosition, x)
  }
}

object Log {

  def apply(c: whitebox.Context): Log = new Log(c)
}