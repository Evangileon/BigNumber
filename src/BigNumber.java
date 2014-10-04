/**
 * @author Jun Yu
 */

import java.util.LinkedList;

public class BigNumber {

	int base;
	LinkedList<Integer> digitList;

	public BigNumber() {
		base = 10;
		digitList = new LinkedList<Integer>();
	}

	public BigNumber(int base) {
		this();
		this.base = base;
	}

	public void printList() {
		System.out.println(digitList);
	}

	/**
	 * Convert char array represented decimal to int
	 * 
	 * @param array
	 * @param start
	 * @param end
	 * @return integer
	 * @throws NumberFormatException
	 */
	public static int charArrayToInt(char[] array, int start, int end)
			throws NumberFormatException {
		int result = 0;
		for (int i = start; i < end; i++) {
			int digit = (int) array[i] - (int) '0';
			if ((digit < 0) || (digit > 9))
				throw new NumberFormatException();
			result *= 10;
			result += digit;
		}
		return result;
	}

    /**
     * BigNumber divided by a integer
     * @param numStr string representation of big number
     * @param intNum delimiter
     * @param buffer to store the string representation of the result
     * @return the remnant of the division
     */
	public int divideByInt(String numStr, int intNum, StringBuffer buffer) {
		String digitMaxStr = String.valueOf(intNum);
		int digitMaxLength = digitMaxStr.length();

		char[] number = numStr.toCharArray();
		int remnant = 0;

		// every iteration extract last digitMaxLength chars of number string.
		// begin at most significant digit
		for (int beginDigit = 0, endDigit = beginDigit + digitMaxLength; endDigit <= number.length;) {

			int tempInt = charArrayToInt(number, beginDigit, endDigit);
			int digit = tempInt / intNum;
			remnant = tempInt % intNum;

			// add to linked list
			buffer.append(digit);

			if (remnant == 0) {
				beginDigit += (endDigit - beginDigit);
				endDigit += (digitMaxLength - 1);
				continue;
			}

			// replace the last digits with remnant
			char[] remnantChars = String.valueOf(remnant).toCharArray();
			// aligned with number (char array)
			for (int i = 0; i < remnantChars.length; i++) {
				number[beginDigit + (endDigit - beginDigit) - remnantChars.length + i] = remnantChars[i];
			}

			beginDigit += ((endDigit - beginDigit) - remnantChars.length);
			endDigit += (digitMaxLength - 1); 
		}
		return remnant;
	}
	
	public void strToNum(String str) {

		String digitMaxStr = String.valueOf(base);
		int digitMaxLength = digitMaxStr.length();

		char[] number = str.toCharArray();
		int remnant = 0;

		// every iteration extract last digitMaxLength chars of number string.
		// begin at most significant digit
		for (int beginDigit = 0, endDigit = beginDigit + digitMaxLength; endDigit <= number.length;) {

			int tempInt = charArrayToInt(number, beginDigit, endDigit);
			int digit = tempInt / base;
			remnant = tempInt % base;

			// add to linked list
			digitList.addFirst(digit);

			if (remnant == 0) {
				beginDigit += (endDigit - beginDigit);
				endDigit += (digitMaxLength - 1);
				continue;
			}

			// replace the last digits with remnant
			char[] remnantChars = String.valueOf(remnant).toCharArray();
			// aligned with number (char array)
			for (int i = 0; i < remnantChars.length; i++) {
				number[beginDigit + (endDigit - beginDigit) - remnantChars.length + i] = remnantChars[i];
			}

			beginDigit += ((endDigit - beginDigit) - remnantChars.length);
			endDigit += (digitMaxLength - 1); 
		}
		digitList.addFirst(remnant);
	}

	public static void main(String[] args) {
		// String str =
		// "90569784495866770974195656280275310090138980613960953881501965823101";
		String str = "972";
		BigNumber big = new BigNumber(13);
		big.strToNum(str);
		big.printList();
	}

}
