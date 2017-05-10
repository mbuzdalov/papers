package onell.util

/**
  * Miscellaneous utilities
  */
object Misc {
  implicit class CommandLineOptions(val args: Array[String]) extends AnyVal {
    def getOption(prefix: String): Option[String] = {
      val idx = args.indexWhere(_.startsWith(prefix))
      if (idx == -1) None else Some(args(idx).substring(prefix.length))
    }
  }
}
