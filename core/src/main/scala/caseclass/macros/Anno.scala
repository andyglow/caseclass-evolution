package caseclass.macros

import scala.reflect.macros._


private[macros] case class Anno(
  from: Universe#Tree,
  remove: Set[String],
  rename: Map[String, String])

private[macros] object Anno {

  def fromTree(c: whitebox.Context)(t: c.universe.Tree)(implicit log: Log): Anno = {
    import c.universe._

    t match {
      case q"new $_(..$params)" =>
        var from: Tree = null
        var remove: Set[String] = null
        var rename: Map[String, String] = null

        def setFrom(v: Tree): Unit = {
          from = c.typecheck(v.asInstanceOf[Tree])
        }

        params foreach {
          case q"from = $v" =>
            setFrom(v.asInstanceOf[Tree])
          case q"removed = $v" =>
            remove = c.eval(c.Expr[Any](q"$v")).asInstanceOf[Set[String]]
          case q"renamed = $v" =>
            rename = c.eval(c.Expr[Any](q"$v")).asInstanceOf[Map[String, String]]
          case p @ q"Set($_)" =>
            remove = c.eval(c.Expr[Any](q"$p")).asInstanceOf[Set[String]]
          case p @ q"Map($_)" =>
            rename = c.eval(c.Expr[Any](q"$p")).asInstanceOf[Map[String, String]]
          case p =>
            setFrom(p.asInstanceOf[Tree])
        }

        Anno(
          from.asInstanceOf[Universe#Tree],
          Option(remove) getOrElse Set.empty,
          Option(rename) getOrElse Map.empty)
      case _ =>
        log.fail(s"can't process ${showCode(t)}")
    }
  }
}
