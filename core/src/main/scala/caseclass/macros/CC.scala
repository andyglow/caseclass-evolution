package caseclass.macros

import scala.reflect.macros._


/** Case Class (taken from Type)
  *
  * @param name
  * @param fields
  * @param parents
  * @param body
  * @param methods
  */
private[macros] case class CC(
  name: String,
  sym: Universe#TypeSymbol,
  fields: Seq[Map[String, (Universe#Tree, Universe#Tree)]],
  parents: Seq[String],
  body: Seq[Universe#Tree],
  methods: Seq[Universe#Tree] = Seq.empty) {

  def withFieldsAdded(c: whitebox.Context)(_fields: Seq[Map[String, (Universe#Tree, Universe#Tree)]]): CC = {
    copy(fields = fields.zip(_fields) map { case (lList, rList) =>
      rList.foldLeft(lList) {
        case (agg, (n, (t, d))) =>
          agg.get(n) match {
            case Some((tt, dd)) => if (t == tt && d == dd) { c.error(c.enclosingPosition, ""); null.asInstanceOf[Nothing] } else agg
            case None => agg updated (n, t -> d)
          }
      }
    })
  }

  def withFieldsRemoved(c: whitebox.Context)(names: Set[String])(implicit log: Log): CC = {
    copy(fields = fields map { args =>
      val fieldsToRemove = args.keySet intersect names
      if (fieldsToRemove != names) {
        val absentFields = names diff fieldsToRemove
        log.err(s"Can not reduce type $name. [${absentFields mkString ", "}] fields were not found")
      }

      args -- fieldsToRemove
    })
  }

  def withMethodsAdded(c: whitebox.Context)(tree: Universe#Tree): CC = {
    copy(methods = methods :+ tree)
  }

  def withFieldsRenamed(c: whitebox.Context)(mapping: Map[String, String])(implicit log: Log): CC = {
    copy(fields = fields map { args =>
      val names = mapping.keySet
      val fieldsToRemove = args.keySet intersect names
      if (fieldsToRemove != names) {
        val absentFields = names diff fieldsToRemove
        log.err(s"Can not modify type $name. [${absentFields mkString ", "}] fields were not found")
      }

      args map {
        case (k, v) =>
          val kk = mapping.getOrElse(k, k)
          (kk, v)
      }
    })
  }

  def gen(c: whitebox.Context): c.Tree = {
    import c.universe._

    val lClassName  = TypeName(name)
    val lBody       = body.asInstanceOf[Seq[Tree]]
    val lFields     = fields map { _.map { case (n, (t, d)) => ValDef(Modifiers(), TermName(n), t.asInstanceOf[Tree], d.asInstanceOf[Tree]) }.toSeq }
    val lParents    = parents map { _.asInstanceOf[Tree]}
    val lMethods    = methods map { _.asInstanceOf[Tree]}

    q"""case class $lClassName ( ...$lFields ) extends ..$lParents {
         ..$lBody
         ..$lMethods
       }
     """
  }
}

private[macros] object CC {

  /** Creates a case class representation from a Type.
    * Used to load data from classes refered in annotation
    *
    * @param c
    * @param cd
    * @return
    */
  def fromType(c: whitebox.Context)(cd: c.universe.Type): CC = {
    import c.universe._

    val ccFields = cd.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists

    val fields: Seq[Map[String, (Universe#Tree, Universe#Tree)]] = ccFields map {
      _.map { s =>
        // TODO: bring default values back
        (s.name.decodedName.toString, q"${s.typeSignature}" -> c.universe.EmptyTree)
      }.toMap
    }

    CC(
      cd.typeSymbol.asClass.name.decodedName.toString,
      cd.typeSymbol.asClass,
      fields,
      Seq.empty,
      Seq.empty)
  }

  /** Creates a case class representation from a Tree
    *
    * @param c
    * @param cd
    * @param log
    * @return
    */
  def fromTree(c: whitebox.Context)(cd: c.universe.ClassDef)(implicit log: Log): CC = {
    import c.universe._

    cd match {
      case q"case class $className(...$fields) extends ..$parents { ..$body }" =>
        val name = className.asInstanceOf[TypeName]

        CC(
          name.decodedName.toString,
          c.internal.newFreeType(name.decodedName.toString),
          fields map {
            case argList: List[_] =>
              argList.map {
                case q"$_ val $n: $t = $d" =>
                  (n.toString, t.asInstanceOf[Universe#Tree] -> d.asInstanceOf[Universe#Tree])
              }.toMap
          },
          parents map { _.toString },
          body map { _.asInstanceOf[Universe#Tree] })
      case _ =>
        log.fail(s"${cd.name} should be a case class")
    }
  }
}
