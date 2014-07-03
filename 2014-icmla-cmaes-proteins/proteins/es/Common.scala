package proteins.es

import java.io.{FileInputStream, File}

import proteins.Structure

/**
 * Common things for main objects.
 * @author Maxim Buzdalov
 */
object Common {
  def readAll(path: String): IndexedSeq[Structure] = {
    val input = new FileInputStream(path)
    val pdb = Structure.read(input, Structure.ResidueWeight)
    input.close()
    pdb
  }
  def readOne(path: String): Structure = {
    val argument = new File(path)
    val (file, conf) = if (argument.exists()) {
      (argument, 0)
    } else {
      val parent = argument.getParentFile
      if (parent.exists() && argument.getName.matches("[0-9]+")) {
        (parent, argument.getName.toInt)
      } else {
        throw new IllegalArgumentException(path + " is not a well formed path to a PDB model")
      }
    }
    readAll(file.getPath)(conf)
  }
}
