package caseclass.macros

import scala.reflect.macros._


object EvolveMacro {

  def impl(c: whitebox.Context)(annottees: Seq[c.Expr[Any]]): c.Expr[Any] = {
    import c.universe._
    implicit val log: Log = Log(c)

    def rewriteCC(x: ClassDef)(fn: CC => CC): Expr[Any] = {
      val s = CC.fromTree(c)(x)
      val res = fn(s).gen(c)

      if (c.settings.contains("print-cc")) {
        log.inf(s"Rewritten CC for type '${s.name}':\n${showCode(res)}")
      }

      c.Expr[Any](res)
    }

    def generate(cd: ClassDef): c.Expr[Any] = {
      val anno = Anno.fromTree(c)(c.prefix.tree)

      val superCC = {
        val className = anno.from.symbol.fullName
        val classType = c. mirror.staticClass(className).toType
        CC.fromType(c)(classType)
      }

      val withSuperMethodName = TermName("with" + superCC.name)

      rewriteCC(cd) { cc =>

        val superKeysToThisKeysMapping = {
          val keys = for {
            argList <- superCC.fields
            key     <- argList.keySet
          } yield key

          val effectiveKeys = keys.toSet -- anno.remove
          effectiveKeys.zip(effectiveKeys).toMap ++ anno.rename
        }

        val withSuperMethod = {
          val mapping =
            for { (l, r) <- superKeysToThisKeysMapping } yield {
              q"${TermName(r)} = x.${TermName(l)}"
            }

          q"def ${withSuperMethodName}(x: ${superCC.sym.asInstanceOf[ClassSymbol]}): ${TypeName(cc.name)} = this.copy(..$mapping)"
        }

        val ccc = superCC
          .copy(name = cc.name, sym = cc.sym)
          .withFieldsAdded(c)(cc.fields)
          .withFieldsRemoved(c)(anno.remove)
          .withFieldsRenamed(c)(anno.rename)
          .withMethodsAdded(c)(withSuperMethod)

        ccc
      }
    }

    annottees.map(_.tree).toList match {
      case (cd: ClassDef) :: Nil => generate(cd)
      case _                     => log.fail("Invalid annottee")
    }
  }
}
