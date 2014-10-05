/**
 * @author Jun Yu
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class BigNumber {

    private final int base;
    LinkedList<Integer> digitList;

    // The larger the better under restriction that the square of base will not overflow
    public static final int optimalBase = 0x7FFF;

    public static BigNumber BigNumberWithOptimalBase() {
        return new BigNumber(optimalBase);
    }

    public BigNumber() {
        this(10);
    }

    public BigNumber(int base) {
        if (base > optimalBase || base <= 0) {
            base = optimalBase;
        }

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
        int digit;

        if (number.length < digitMaxLength) {
            // It number.length, < digitMaxLength, that means
            // the digit is zero, and the whole number equals to remnant after division
            remnant = charArrayToInt(number, 0, number.length);
            buffer.append("0");
            return remnant;
        }

        if (number.length == digitMaxLength) {
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
            System.arraycopy(remnantChars, 0, number, beginDigit + (endDigit - beginDigit) - remnantChars.length, remnantChars.length);

            // move indices
            beginDigit += ((endDigit - beginDigit) - remnantChars.length);
            // move to one higher significant digit
            endDigit += 1;
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
        for (Integer digit : digits) {
            if (!result.equals("0")) {
                // if result != "0", multiplied by 10, that is appended a "0"
                result = result + "0";
            }

            String temp = multiplyBySingleDigit(numStr, digit);
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
     *
     * @param dight to be appended
     */
    private void addDigit(int dight) {
        if(dight < 0) {
            return;
        }
        this.digitList.add(dight);
    }

    /**
     * Equivalent to multiply (base * numBase) to this,
     * that is to prepend numBase 0 digits into digitList
     * @param numBase the number of node prepend to list
     */
    private void shiftByBase(int numBase) {
        if (numBase <= 0) {
            return;
        }

        for (int i = 0; i < numBase; i++) {
            this.getDigitList().addFirst(0);
        }
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

    /**
     * Subtract this to other big numbrt
     * @param other big number
     * @return the difference of two big numbers, if less than 0, return 0
     */
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

            if(itor1.hasNext()) {
                result.addDigit(temp);
            }
        }

        return result;
    }

    /**
     * This big number multiplied with a integer
     * @param other integer
     * @return big number product
     */
    public BigNumber multiply(int other) {
        if(other < 0) {
            return this;
        }

        BigNumber result = new BigNumber(this.base);

        int carry = 0;
        int digit;

        for (Integer integer : this.getDigitList()) {
            digit = integer;

            int temp = digit * other + carry;

            if (temp > (this.base - 1)) {
                carry = temp / this.base;
                temp = temp % this.base;
            } else {
                carry = 0;
            }

            result.addDigit(temp);
        }

        // Don't forget the last carry
        if(carry > 0) {
            result.addDigit(carry);
        }

        return result;
    }

    /**
     * This big number multiplied with a big number
     * @param other big number
     * @return big number product
     */
    public BigNumber multiply(BigNumber other) {
        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        BigNumber result = new BigNumber(this.base);

        // carry can be non-negative integer
        int carry = 0;
        BigNumber left = this;
        BigNumber right = other;
        int digit2;

        // To reduce running time, the right operand should be the one with less digit list
        if(this.getDigitList().size() < other.getDigitList().size()) {
            BigNumber swap = left;
            left = right;
            right = swap;
        }

        // Resemble to the human steps of calculate the product of two number,
        // first get one digit of other operand, then multiply with this,
        // if other has more digit, multiply the previous with base
        // and add with the product of new digit and this
        Iterator<Integer> itor2 = right.getDigitList().descendingIterator();
        while (itor2.hasNext()) {
            digit2 = itor2.next();

            BigNumber temp = left.multiply(digit2);
            //System.out.println("D " + temp.numToStr());
            result = result.add(temp);

            if (itor2.hasNext()) {
                // multiplied by base
                // add one node at the head of digitList with value 0
                result.shiftByBase(1);
            }
        }

        return result;
    }

    public BigNumber power(BigNumber other) {
        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        BigNumber result = new BigNumber(this.base);

        System.out.println(result.numToStr());

        return result;
    }

    public static void executeLoop() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // store var names and their correspondent big number
        HashMap<String, BigNumber> varMap = new HashMap<String, BigNumber>();
        // store line number and expr
        ArrayList<String> exprTable = new ArrayList<String>();
        // exprTable[0] unused
        exprTable.add("");

        // read expr from stdin
        String line;
        try {
            while((line = reader.readLine()) != null) {
                String[] params = line.split(" ");
                if(params.length != 2) {
                    System.out.println("Invalid input: wrong format");
                    return;
                }

                int lineNum = Integer.parseInt(params[0]);
                String expr = params[1];
                // set expr table for jumping
                if (exprTable.get(lineNum) == null) {
                    exprTable.add(expr);
                } else {
                    exprTable.set(lineNum, expr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // begin to handle expr
        for (int i = 1; i < exprTable.size(); i++) {
            String expr = exprTable.get(i);

            // first handle ? jump
            if (expr.contains("?")) {
                String[] jumpParams = expr.split("\\?");
                if(jumpParams.length != 2) {
                    System.out.println("Invalid input: wrong jump");
                    return;
                }


            }
        }
    }

    public static int specifiedBase = optimalBase;

    public static void main(String[] args) {
        if(args.length > 0) {
            specifiedBase = Integer.parseInt(args[0]);
            System.out.println("Specified base = " + specifiedBase);
        } else {
            System.out.println("Default base = " + BigNumber.optimalBase);
        }

        //String str = "90569784495866770974195656280275310090138980613960953881501965823101";
        String str = "769";
        //BigNumber big = BigNumber.BigNumberWithOptimalBase();
        BigNumber big = new BigNumber(13);
        //String str2 = "75040970647524038461398929683905540248523961720824412136973299943953";
        String str2 = "730";
        //BigNumber big2 = BigNumber.BigNumberWithOptimalBase();
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
        //System.out.println(big.add(big2).numToStr());
        BigNumber result = big.substract(big2);
        result.printList();
        System.out.println(result.numToStr());
        //System.out.println(big.multiply(13).numToStr());
        //System.out.println(big.multiply(big2).numToStr());
        //Math.pow(12, 22);
    }
}
