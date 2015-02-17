package org.data2semantics.mustard.rdfvault;
/**
 * This exception is thrown when you attempt to call one of the {@link Vault} methods
 * {@link Vault#redeem redeem} or {@link Vault#trash trash} on a ticket that has previously been trashed,
 * or on an object that is not a ticket of the vault.
 * 
 * @author Steven de Rooij
 */

public class InvalidTicketException extends RuntimeException {

	private static final long serialVersionUID = 2371623906872717292L;

	public InvalidTicketException() { super("Attempt to use an invalid ticket"); }

}
