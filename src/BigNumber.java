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

    public int getBase() {
        return base;
    }

    public LinkedList<Integer> getDigitList() {
        return digitList;
    }

    /**
     * Convert char array represented decimal to int
     *
     * @param array input char array
     * @param start index inclusive
     * @param end   index exclusive
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
     *
     * @param numStr string representation of big number
     * @param intNum delimiter
     * @param buffer to store the string representation of the result
     * @return the remnant of the division
     */
    public int divideByInt(String numStr, int intNum, StringBuffer buffer) {
        String digitMaxStr = String.valueOf(intNum);
        // The max length of chars that one iteration of division will affect
        int digitMaxLength = digitMaxStr.length();

        char[] number = numStr.toCharArray();
        int remnant = 0;
        int digit = 0;

        if (number.length < digitMaxLength) {
            // It number.length, < digitMaxLength, that means
            // the digit is zero, and the whole number equals to remnant after division
            remnant = charArrayToInt(number, 0, number.length);
            buffer.append("0");
            return remnant;
        }

        if(number.length == digitMaxLength) {
            // It the length equal, then compare the value
            // after arithmetic operation, return append digit and return remnant immediately
            // this can reduce running time
            int tempInt = charArrayToInt(number, 0, number.length);
            digit = tempInt / intNum;
            remnant = tempInt % intNum;
            buffer.append(digit);
            return remnant;
        }

        boolean firstZero = true;
        // every iteration extract last digitMaxLength chars of number string.
        // begin at most significant digit
        for (int beginDigit = 0, endDigit = beginDigit + digitMaxLength; endDigit <= number.length; ) {

            int tempInt = charArrayToInt(number, beginDigit, endDigit);
            digit = tempInt / intNum;
            remnant = tempInt % intNum;

            // add to linked list
            if (digit != 0) {
                firstZero = false;
            }
            if (digit != 0 || !firstZero) {
                // It the first several digit is zero, don't append to result
                // It just begin at the first non zero digit occurs
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

            // move indices
            beginDigit += ((endDigit - beginDigit) - remnantChars.length);
            endDigit += (digitMaxLength - 1);
        }
        return remnant;
    }

    /**
     * A string representative number (no limitation) multiplied by a single digit integer
     *
     * @param numStr string representative number
     * @param digit  single digit integer
     * @return the result if single digit, otherwise itseld
     */
    public String multiplyBySingleDigit(String numStr, int digit) {
        if (digit > 9) {
            return numStr;
        }

        char[] result = new char[numStr.length() + 1];
        char[] num = numStr.toCharArray();
        int i = num.length - 1;
        int k = result.length - 1;
        int carry = 0;
        while (i >= 0) {
            int temp = (num[i] - '0') * digit + carry;
            if (temp > 9) {
                // temp > 9. means there is a carry
                carry = temp / 10;
                temp = temp % 10;
            } else {
                carry = 0;
            }

            result[k] = (char) (temp + '0');
            i--;
            k--;
        }

        if (carry > 0) {
            // It on the most significant, there is a carry, them the result fit the array completely.
            // result[0] is used
            result[0] = (char) (carry + '0');
            return new String(result);
        }

        // Otherwise result[0] is never used, truncate it out
        return new String(result, 1, result.length - 1);
    }

    /**
     * Collect every digit of an integer, and store them into List, by the order of significance
     *
     * @param num    non-negative integer
     * @param digits List to store digits
     */
    private void collectDigits(int num, ArrayList<Integer> digits) {
        if (num / 10 > 0) {
            collectDigits(num / 10, digits);
        }
        digits.add(num % 10);
    }

    public String multiplyByInt(String numStr, int intNum) {
        // get every digit of intNum
        ArrayList<Integer> digits = new ArrayList<Integer>();
        collectDigits(intNum, digits);

        String result = "0";
        for (int i = 0; i < digits.size(); i++) {
            if (!result.equals("0")) {
                // if result != "0", multiplied by 10, that is appended a "0"
                result = result + "0";
            }

            String temp = multiplyBySingleDigit(numStr, digits.get(i));
            // result * 10 + temp
            result = addTwoNumStr(result, temp);
        }

        return result;
    }

    /**
     * Return the result of the sum of two char array representative numbers
     *
     * @param array1 left operand
     * @param array2 right operand
     * @return result
     */
    public String addTwoNumStr(char[] array1, char[] array2) {
        int i = array1.length - 1;
        int j = array2.length - 1;

        // The size of the result should be the max of two length,
        // plus one digit for most significant carry, if needed.
        char[] result = new char[Math.max(i, j) + 2];
        int k = result.length - 1;
        int carry = 0; // initialized to 0

        // For tail to head
        while (i >= 0 || j >= 0) {
            // If i and exceed the range of two arrays, let them be zero
            char temp1 = (i >= 0) ? array1[i] : '0';
            char temp2 = (j >= 0) ? array2[j] : '0';
            // digit of array1 + digit of array2 + carry
            int tmpResult = (temp1 - '0') + (temp2 - '0') + carry;
            if (tmpResult > 9) {
                carry = 1;
                tmpResult = tmpResult % 10;
            } else {
                carry = 0;
            }
            result[k] = (char) (tmpResult + '0');
            i--;
            j--;
            k--;
        }

        if (carry == 1) {
            // It on the most significant, there is a carry, them the result fit the array completely.
            // result[0] is used
            result[0] = (char) (1 + '0');
            return new String(result);
        }

        // Otherwise result[0] is never used, truncate it out
        return new String(result, 1, result.length - 1);
    }

    /**
     * Return the result of the sum of two string representative numbers, actually it's a wrapper of
     * String addTwoNumStr(char[] array1, char[] array2)
     *
     * @param num1 left operand
     * @param num2 right operand
     * @return result
     */
    public String addTwoNumStr(String num1, String num2) {
        char[] array1 = num1.toCharArray();
        char[] array2 = num2.toCharArray();
        return addTwoNumStr(array1, array2);
    }

    /**
     * String to big number
     *
     * @param str the string representative decimal
     */
    public void strToNum(String str) {
        int remnant;
        String num = str;

        while (!num.equals("0")) {
            // If num = "0", means the result of division is 0, so the loop ends
            StringBuffer buffer = new StringBuffer();
            remnant = divideByInt(num, this.base, buffer);
            num = buffer.toString();
            //System.out.println(num);
            // remnant is the content of every node
            digitList.add(remnant);
        }
    }

    /**
     * Big number to string
     *
     * @return the string representative decimal
     */
    public String numToStr() {
        Iterator<Integer> itor = this.digitList.descendingIterator();
        String result = "0";
        while (itor.hasNext()) {
            int digit = itor.next();
            String temp = String.valueOf(digit);

            if (itor.hasNext()) {
                // escape last one, so that the result be correct
                // you should choose base wisely so that the operation won't overflow
                result = multiplyByInt(result, this.base);
                // multiplied by base
                temp = String.valueOf(this.base * digit);
            }
            // add result * base + temp
            result = addTwoNumStr(result, temp);
        }

        return result;
    }

    /**
     * Append a digit to this big number, this is for only internal operation
     * @param dight to be appended
     */
    private void addDigit(int dight) {
        this.digitList.add(Integer.valueOf(dight));
    }

    /**
     * Add this with a big number, is their base not the same, throw a exception
     *
     * @param other right operand
     * @return the result of addition
     */
    public BigNumber add(BigNumber other) {
        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        BigNumber result = new BigNumber(this.base);
        int carry = 0;
        Iterator<Integer> itor1 = this.getDigitList().iterator();
        Iterator<Integer> itor2 = other.getDigitList().iterator();
        int digit1, digit2;

        while (itor1.hasNext() || itor2.hasNext()) {
            if (itor1.hasNext()) {
                // If itor exceed the range, let it be zero
                digit1 = itor1.next();
            } else {
                digit1 = 0;
            }
            if (itor2.hasNext()) {
                digit2 = itor2.next();
            } else {
                digit2 = 0;
            }

            // if base is less than 2 ^ 15, the operation will never overflow
            int temp = digit1 + digit2 + carry;
            if (temp > (this.base - 1)) {
                carry = 1;
                temp = temp % this.base;
            } else {
                carry = 0;
            }

            result.addDigit(temp);
        }

        if (carry == 1) {
            // last carry
            result.addDigit(1);
        }

        return result;
    }

    public BigNumber substract(BigNumber other) {
        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        BigNumber result = new BigNumber(this.base);
        // borrow from higher digit, if current digit is less than others current digit
        int borrow = 0;
        Iterator<Integer> itor1 = this.getDigitList().iterator();
        Iterator<Integer> itor2 = other.getDigitList().iterator();
        int digit1, digit2;

        // Because this project don't handle negative number,
        // the number of digit of this is always large than or equal to others
        // so only need care about the other digit
        while (itor1.hasNext() || itor2.hasNext()) {
            if (itor1.hasNext()) {
                // If itor exceed the range, let it be zero
                digit1 = itor1.next();
            } else {
                digit1 = 0;
            }
            if (itor2.hasNext()) {
                digit2 = itor2.next();
            } else {
                digit2 = 0;
            }

            int temp = digit1 - (digit2 + borrow);

            if (temp < 0) {
                // then digit borrow 1 from higher digit of this big number
                borrow = 1;
                temp = temp + this.base;
            } else {
                borrow = 0;
            }

            result.addDigit(temp);
        }

        /*
        if (borrow == 1) {
            // most significant digit of iterator 2 may affect
            if (!itor1.hasNext()) {
                throw new NumberFormatException("this is less than other, " +
                        "that returns negative number, " +
                        "which is not required in this project");
            }

            int lastDigit = itor1.next();
            //result.addDigit(la);
        }*/

        return result;
    }

    public static void main(String[] args) {
        String str = "90569784495866770974195656280275310090138980613960953881501965823101";
        //String str = "769";
        BigNumber big = new BigNumber(13);
        String str2 = "182";
        BigNumber big2 = new BigNumber(13);
        big.strToNum(str);
        big2.strToNum(str2);
        big.printList();
        big2.printList();
        System.out.println(big.numToStr());
        System.out.println(big2.numToStr());
        //StringBuffer buffer = new StringBuffer();
        //System.out.println(big.divideByInt("0", 13, buffer));
        //System.out.println(buffer.toString());
        //System.out.println("" + Integer.MAX_VALUE);
        //System.out.println(big.multiplyByInt("1232", 51));
        System.out.println(big.add(big2).numToStr());
        BigNumber result = big.substract(big2);
        result.printList();
        System.out.println(result.numToStr());
    }
}
