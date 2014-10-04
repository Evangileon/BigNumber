/**
 * @author Jun Yu
 */

import java.util.LinkedList;

public class BigNumber {

	private final int base;
	LinkedList<Integer> digitList;

	public BigNumber() {
		this(10);
	}

	public BigNumber(int base) {
		this.base = base;
        digitList = new LinkedList<Integer>();
	}

	public void printList() {
		System.out.println(digitList);
	}

	/**
	 * Convert char array represented decimal to int
	 * 
	 * @param array input char array
	 * @param start index inclusive
	 * @param end index exclusive
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

        if(number.length < digitMaxLength) {
            remnant = charArrayToInt(number, 0, number.length);
            buffer.append("0");
            return remnant;
        }

        boolean firstzero = true;
		// every iteration extract last digitMaxLength chars of number string.
		// begin at most significant digit
		for (int beginDigit = 0, endDigit = beginDigit + digitMaxLength; endDigit <= number.length;) {

			int tempInt = charArrayToInt(number, beginDigit, endDigit);
			int digit = tempInt / intNum;
			remnant = tempInt % intNum;

			// add to linked list
            if(digit != 0) {
                firstzero = false;
            }
            if(digit != 0 || !firstzero) {
                buffer.append(digit);
            }

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
		int remnant;
        String num = str;

        while(!num.equals("0")) {
            StringBuffer buffer = new StringBuffer();
            remnant = divideByInt(num, this.base, buffer);
            num = buffer.toString();
            //System.out.println(num);
            digitList.add(remnant);
        }
	}

	public static void main(String[] args) {
		String str = "90569784495866770974195656280275310090138980613960953881501965823101";
		//String str = "769";
		BigNumber big = new BigNumber(13);
		big.strToNum(str);
		big.printList();
        StringBuffer buffer = new StringBuffer();
        System.out.println(big.divideByInt("0", 13, buffer));
        System.out.println(buffer.toString());
        System.out.println("" + Integer.MAX_VALUE);
    }
}
