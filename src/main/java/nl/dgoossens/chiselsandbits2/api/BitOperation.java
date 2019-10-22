package nl.dgoossens.chiselsandbits2.api;

/**
 * The operation performed using a chisel.
 */
public enum BitOperation {
    PLACE,
    SWAP, //Swap removes the current bit and replaces it with the new bit. (combo of PLACE/REMOVE)
    REMOVE
}
