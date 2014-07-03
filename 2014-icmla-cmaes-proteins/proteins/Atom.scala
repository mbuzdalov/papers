package proteins

/**
 * An atom with the associated weight.
 * @param location the location of the atom.
 * @param weight the weight of the atom.
 * @param kind the kind of the atom.
 * @param aminoAcid the amino acid the atom belongs to.
 */
case class Atom(location: Point3, weight: Double, kind: String, aminoAcid: String)
