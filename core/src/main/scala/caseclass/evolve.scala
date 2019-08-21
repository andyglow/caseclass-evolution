package caseclass


import scala.reflect.macros.whitebox
import scala.annotation._

@compileTimeOnly("enable macro paradise to expand macro annotations")
class evolve(
  from: Any,
  removed: Set[String] = Set.empty,
  renamed: Map[String, String] = Map.empty) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro evolve.impl
}

private object evolve {
  import caseclass.macros._

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = EvolveMacro.impl(c)(annottees)
}