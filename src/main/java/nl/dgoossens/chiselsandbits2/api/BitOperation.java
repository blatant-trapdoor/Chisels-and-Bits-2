package nl.dgoossens.chiselsandbits2.api;

/**
 * The operation performed using a chisel.
 */
public enum BitOperation {
    PLACE,
    REPLACE, //Place and replace can be toggled between using buttons in the menu.
    REMOVE
}
