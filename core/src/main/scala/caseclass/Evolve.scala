package caseclass


import scala.reflect.macros.whitebox
import scala.annotation._

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Evolve(
  from: Any,
  removed: Set[String] = Set.empty,
  renamed: Map[String, String] = Map.empty) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Evolve.impl
}

private object Evolve {
  import caseclass.macros._

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = EvolveMacro.impl(c)(annottees)
}