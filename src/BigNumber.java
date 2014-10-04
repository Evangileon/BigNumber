/**
 * @author Jun Yu
 */

import java.util.ArrayList;
import java.util.Iterator;
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

        boolean firstZero = true;
		// every iteration extract last digitMaxLength chars of number string.
		// begin at most significant digit
		for (int beginDigit = 0, endDigit = beginDigit + digitMaxLength; endDigit <= number.length;) {

			int tempInt = charArrayToInt(number, beginDigit, endDigit);
			int digit = tempInt / intNum;
			remnant = tempInt % intNum;

			// add to linked list
            if(digit != 0) {
                firstZero = false;
            }
            if(digit != 0 || !firstZero) {
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

    public String multiplyBySingleDigit(String numStr, int digit) {
        if(digit > 9) {
            return numStr;
        }

        char[] result = new char[numStr.length() + 1];
        char[] num = numStr.toCharArray();
        int i = num.length - 1;
        int k = result.length - 1;
        int carry = 0;
        while(i >= 0) {
            int temp = (num[i] - '0') * digit + carry;
            if(temp > 9) {
                carry = temp / 10;
                temp = temp % 10;
            } else {
                carry = 0;
            }

            result[k] = (char)(temp + '0');
            i--;
            k--;
        }

        if(carry > 0) {
            result[0] = (char)(carry + '0');
            return new String(result);
        }

        return new String(result, 1, result.length - 1);
    }

    private void collectDigits(int num, ArrayList<Integer> digits) {
        if(num / 10 > 0) {
            collectDigits(num / 10, digits);
        }
        digits.add(num % 10);
    }

    public String multiplyByInt(String numStr, int intNum) {
        ArrayList<Integer> digits = new ArrayList<Integer>();
        collectDigits(intNum, digits);

        String result = "0";
        for (int i = 0; i < digits.size(); i++) {
            if(!result.equals("0")) {
                result = result + "0";
            }

            String temp = multiplyBySingleDigit(numStr, digits.get(i));
            result = addTwoNumStr(result, temp);
        }

        return result;
    }

    public String addTwoNumStr(char[] array1, char[] array2) {
        int i = array1.length - 1;
        int j = array2.length - 1;

        char[] result = new char[Math.max(i, j) + 2];
        int k = result.length - 1;
        int carry = 0;

        while(i >= 0 || j >= 0) {
            char temp1 = (i >= 0) ? array1[i] : '0';
            char temp2 = (j >= 0) ? array2[j] : '0';
            int tmpResult = (temp1 - '0') + (temp2 - '0') + carry;
            if(tmpResult > 9) {
                carry = 1;
                tmpResult = tmpResult % 10;
            } else {
                carry = 0;
            }
            result[k] = (char)(tmpResult + '0');
            i--;
            j--;
            k--;
        }

        if(carry == 1) {
            result[0] = (char)(1 + '0');
            return new String(result);
        }

        return new String(result, 1, result.length - 1);
    }

    public String addTwoNumStr(String num1, String num2) {
        char[] array1 = num1.toCharArray();
        char[] array2 = num2.toCharArray();
        return addTwoNumStr(array1, array2);
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

    public String numToStr() {
        Iterator<Integer> itor = this.digitList.descendingIterator();
        String result = "0";
        while (itor.hasNext()) {
            int digit = itor.next();
            String temp = String.valueOf(digit);

            if(itor.hasNext()) {
                // you should choose base wisely so that the operation won't overflow
                result = multiplyByInt(result, this.base);
                temp = String.valueOf(this.base * digit);
            }
            result = addTwoNumStr(result, temp);
        }

        return result;
    }

	public static void main(String[] args) {
		String str = "90569784495866770974195656280275310090138980613960953881501965823101";
		//String str = "769";
		BigNumber big = new BigNumber(13);
		big.strToNum(str);
		big.printList();
        System.out.println(big.numToStr());
        StringBuffer buffer = new StringBuffer();
        System.out.println(big.divideByInt("0", 13, buffer));
        System.out.println(buffer.toString());
        System.out.println("" + Integer.MAX_VALUE);
        System.out.println(big.multiplyByInt("1232", 51));
    }
}
