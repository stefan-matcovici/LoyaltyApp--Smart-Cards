/**
 * 
 */
package ro.uaic.info.sca.loyaltyapp;

import javacard.framework.*;
import javacardx.annotations.*;
import static ro.uaic.info.sca.loyaltyapp.LoyaltyAppStrings.*;

/**
 * Applet class
 * 
 * @author <user>
 */
@StringPool(value = { @StringDef(name = "Package", value = "ro.uaic.info.sca.loyaltyapp"),
		@StringDef(name = "AppletName", value = "LoyaltyApp") },
		// Insert your strings here
		name = "LoyaltyAppStrings")
public class LoyaltyApp extends Applet {

	/* constants declaration */

	// code of CLA byte in the command APDU header
	final static byte Wallet_CLA = (byte) 0x80;

	// codes of INS byte in the command APDU header
	final static byte VERIFY = (byte) 0x20;
	final static byte CREDIT = (byte) 0x30;
	final static byte DEBIT = (byte) 0x40;
	final static byte GET_BALANCE = (byte) 0x50;
	final static byte UPDATE_PIN = (byte) 0x70;

	// maximum balance
	final static short MAX_BALANCE = 0x7FFF;
	// maximum transaction amount
	final static short MAX_TRANSACTION_AMOUNT = 600;

	// maximum number of incorrect tries before the
	// PIN is blocked
	final static byte PIN_TRY_LIMIT = (byte) 0x03;
	// maximum size PIN
	final static byte MAX_PIN_SIZE = (byte) 0x08;

	// signal that the PIN verification failed
	final static short SW_VERIFICATION_FAILED = 0x6300;
	// signal the the PIN validation is required
	// for a credit or a debit transaction
	final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;
	// signal invalid transaction amount
	// amount > MAX_TRANSACTION_AMOUNT or amount < 0
	final static short SW_INVALID_TRANSACTION_AMOUNT = 0x6A83;

	// signal that the balance exceed the maximum
	final static short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;
	// signal the the balance becomes negative
	final static short SW_NEGATIVE_BALANCE = 0x6A85;

	/* instance variables declaration */
	OwnerPIN pin;
	short balance;
	short tries = 0;

	/**
	 * Only this class's install method should create the applet object.
	 */
	protected LoyaltyApp(byte[] bArray, short bOffset, byte bLength) {
		// It is good programming practice to allocate
		// all the memory that an applet needs during
		// its lifetime inside the constructor
		pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);

		byte iLen = bArray[bOffset]; // aid length
		bOffset = (short) (bOffset + iLen + 1);
		byte cLen = bArray[bOffset]; // info length
		bOffset = (short) (bOffset + cLen + 1);
		byte aLen = bArray[bOffset]; // applet data length

		// The installation parameters contain the PIN
		// initialization value
		pin.update(bArray, (short) (bOffset + 1), aLen);
		register();
	}

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// create a Wallet applet instance
		new LoyaltyApp(bArray, bOffset, bLength);
	} // end of install method

	@Override
	public boolean select() {

		// The applet declines to be selected
		// if the pin is blocked.
		if (pin.getTriesRemaining() == 0) {
			return false;
		}

		return true;

	}// end of select method

	/**
	 * Processes an incoming APDU.
	 * 
	 * @see APDU
	 * @param apdu
	 *            the incoming APDU
	 */
	@Override
	public void process(APDU apdu) {
		// Insert your code here
		// APDU object carries a byte array (buffer) to
		// transfer incoming and outgoing APDU header
		// and data bytes between card and CAD

		// At this point, only the first header bytes
		// [CLA, INS, P1, P2, P3] are available in
		// the APDU buffer.
		// The interface javacard.framework.ISO7816
		// declares constants to denote the offset of
		// these bytes in the APDU buffer

		byte[] buffer = apdu.getBuffer();
		// check SELECT APDU command

		if (apdu.isISOInterindustryCLA()) {
			if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
				return;
			}
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		// verify the reset of commands have the
		// correct CLA byte, which specifies the
		// command structure
		if (buffer[ISO7816.OFFSET_CLA] != Wallet_CLA) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		switch (buffer[ISO7816.OFFSET_INS]) {
		case GET_BALANCE:
			getBalance(apdu);
			return;
		case DEBIT:
			debit(apdu);
			return;
		case CREDIT:
			credit(apdu);
			return;
		case VERIFY:
			verify(apdu);
			return;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	private void verify(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// retrieve the PIN data for validation.
		byte byteRead = (byte) (apdu.setIncomingAndReceive());

		// check pin
		// the PIN data is read into the APDU buffer
		// at the offset ISO7816.OFFSET_CDATA
		// the PIN data length = byteRead
		if (pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
			ISOException.throwIt(SW_VERIFICATION_FAILED);
		}
	}

	private void credit(APDU apdu) {
		// TODO Auto-generated method stub

	}

	private void debit(APDU apdu) {
		
	}

	private void getBalance(APDU apdu) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deselect() {

		// reset the pin value
		pin.reset();

	}
}
