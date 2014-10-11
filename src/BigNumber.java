/**
 * @author Jun Yu
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class BigNumber implements Comparable<BigNumber> {

    private final int base;
    private LinkedList<Integer> digitList;
    private boolean negative = false;

    // The larger the better under restriction that the square of base will not overflow
    public static final int optimalBase = 0x7FFF;
    // the number of the integers that each list node contains
    public static final int digitsPerNode = 100;

    private static int specifiedBase = optimalBase;

    public static void setSpecifiedBase(int specifiedBase) {
        BigNumber.specifiedBase = specifiedBase;
    }

    public static int getSpecifiedBase() {
        return specifiedBase;
    }

    public static BigNumber BigNumberWithOptimalBase() {
        return new BigNumber(optimalBase);
    }

    public BigNumber() {
        this(specifiedBase);
    }

    public BigNumber(int base) {
        if (base > optimalBase || base <= 0) {
            base = optimalBase;
        }

        this.base = base;
        digitList = new LinkedList<Integer>();
    }

    public BigNumber(BigNumber other) {
        this(other.getBase());
        this.getDigitList().addAll(other.getDigitList());
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

    public boolean isNegative() {
        return this.negative;
    }

    private void negate() {
        if (!this.isZero()) {
            negative = !negative;
        } else {
            negative = false;
        }
    }

    /**
     * Trim the zeros at tail of linked list
     */
    private void trimTopZeros() {
        Iterator<Integer> itor = this.digitDescendingIterator();

        while (itor.hasNext()) {
            if (itor.next() == 0) {
                itor.remove();
            } else {
                return;
            }
        }

        if (this.getDigitList().size() == 0) {
            this.negative = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BigNumber bigNumber = (BigNumber) o;

        return base == bigNumber.base && digitList.equals(bigNumber.digitList);
    }

    @Override
    public int hashCode() {
        int result = base;
        result = 31 * result + digitList.hashCode();
        return result;
    }

    public Iterator<Integer> digitIterator() {
        return new DigitIterator(digitList);
    }

    class DigitIterator implements Iterator<Integer> {

        LinkedList<Integer> digitAdhoc;
        Iterator<Integer> itor;

        DigitIterator(LinkedList<Integer> digitAdhoc) {
            this.digitAdhoc = digitAdhoc;
            if (this.digitAdhoc == null) {
                this.digitAdhoc = new LinkedList<Integer>();
            }
            this.itor = this.digitAdhoc.iterator();
        }

        @Override
        public boolean hasNext() {
            return itor.hasNext();
        }

        @Override
        public Integer next() {
            return itor.next();
        }

        @Override
        public void remove() {
            itor.remove();
        }
    }

    public Iterator<Integer> digitDescendingIterator() {
        return new DigitDescendingIterator(digitList);
    }

    class DigitDescendingIterator implements Iterator<Integer> {

        LinkedList<Integer> digitAdhoc;
        Iterator<Integer> itor;

        DigitDescendingIterator(LinkedList<Integer> digitAdhoc) {
            this.digitAdhoc = digitAdhoc;
            if (this.digitAdhoc == null) {
                this.digitAdhoc = new LinkedList<Integer>();
            }
            this.itor = this.digitAdhoc.descendingIterator();
        }

        @Override
        public boolean hasNext() {
            return itor.hasNext();
        }

        @Override
        public Integer next() {
            return itor.next();
        }

        @Override
        public void remove() {
            itor.remove();
        }
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
    private static int charArrayToInt(char[] array, int start, int end)
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
     * This big number divided by integer
     *
     * @param divisor delimiter
     * @param buffer for output
     * @return remnant
     */
    private int divideByInt(int divisor, StringBuffer buffer) {
        String str = this.numToStr();
        return strDivideByInt(str, divisor, buffer);
    }

    private void swapBigNumbers(BigNumber other) {
        LinkedList<Integer> swap = this.digitList;
        this.digitList = other.digitList;
        other.digitList = swap;
    }

    /**
     * Big number divided by integer
     * @param divisor integer
     * @param result for output the result of division, you have to initialize it in caller before calling
     * @return remnant
     */
    public int divideByInt(int divisor, BigNumber result) {
        if(divisor == 0) {
            throw new IllegalArgumentException("Argument 'divisor' is 0");
        }

        // for abs of divisor < base
        // for convenience, divisor will be converted to its absolute
        boolean isResultNegative = false;

        if ((divisor < 0) ^ (this.isNegative())) {
            isResultNegative = true;
        } else {
            isResultNegative = false;
        }

        if (this.isZero()) {
            isResultNegative = false;
        }

        divisor = Math.abs(divisor);

        if (Math.abs(divisor) >= this.base) {
            // big divisor
            StringBuffer buffer = new StringBuffer();

            String str = this.absoluteNumToStr();
            int remnant = strDivideByInt(str, divisor, buffer);
            BigNumber temp = new BigNumber();
            temp.strToNum(buffer.toString());
            result.swapBigNumbers(temp);

            if (isResultNegative) {
                result.negative = true;
            }
            return remnant;
        }


        boolean thisNeedNegateAtLast = false;
        if (this.isNegative()) {
            thisNeedNegateAtLast = true;
            this.negate();
        }

        // TODO everything
        int newDigit;
        int remnant = 0;
        boolean firstZero = true;

        for(Iterator<Integer> itor = this.digitDescendingIterator(); itor.hasNext();) {
            int digit = itor.next();
            newDigit = (digit + remnant * this.base) / divisor;
            remnant = digit % divisor;

            result.addFirstDigit(digit);
        }

        if (thisNeedNegateAtLast) {
            this.negate();
        }

        if(isResultNegative) {
            result.negate();
        }
        result.trimTopZeros();

        return remnant;
    }

    /**
     * String representative big number divided by a integer, only accept string without '-'
     *
     * @param numStr string representation of big number
     * @param divisor delimiter
     * @param buffer to store the string representation of the result
     * @return the remnant of the division
     */
    public int strDivideByInt(String numStr, int divisor, StringBuffer buffer) {
        if(numStr.charAt(0) == '-') {
            throw new IllegalArgumentException("numStr can not be positive");
        }


        String digitMaxStr = String.valueOf(divisor);
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
            digit = tempInt / divisor;
            remnant = tempInt % divisor;
            buffer.append(digit);
            return remnant;
        }

        boolean firstZero = true;
        // every iteration extract last digitMaxLength chars of number string.
        // begin at most significant digit
        for (int beginDigit = 0, endDigit = beginDigit + digitMaxLength; endDigit <= number.length; ) {

            int tempInt = charArrayToInt(number, beginDigit, endDigit);
            digit = tempInt / divisor;
            remnant = tempInt % divisor;

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
                // if remnant is 0, there is no need to replace chars in original char arrays
                // just move a window which width is (digitMaxLength - 1) to right
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
     * @return the result if single digit, otherwise itself
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
        boolean isResultNegative = false;
        if (str.charAt(0) == '-') {
            str =  str.substring(1);
            isResultNegative = true;
        }

        int remnant;
        String num = str;

        while (!num.equals("0")) {
            // If num = "0", means the result of division is 0, so the loop ends
            StringBuffer buffer = new StringBuffer();
            remnant = strDivideByInt(num, this.base, buffer);
            num = buffer.toString();
            //System.out.println(num);
            // remnant is the content of every node
            addDigit(remnant);
        }

        if (isResultNegative) {
            this.negative = true;
        }
    }

    /**
     * Big number to string
     *
     * @return the string representative decimal
     */
    public String numToStr() {
        String absoluteResult = this.absoluteNumToStr();

        if(this.isNegative()) {
            return "-" + absoluteResult;
        }
        return absoluteResult;
    }

    /**
     * Absolute of big number to string
     *
     * @return the string representative decimal
     */
    public String absoluteNumToStr() {
        Iterator<Integer> itor = this.digitDescendingIterator();
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
     * @param digit to be appended
     */
    private void addDigit(int digit) {
        if (digit < 0) {
            return;
        }
        this.digitList.add(digit);
    }

    /**
     * Prepend a digit to this big number, this is for only internal operation
     * @param digit
     */
    private void addFirstDigit(int digit) {
        if (digit < 0) {
            return;
        }
        this.digitList.addFirst(digit);
    }

    /**
     * Equivalent to multiply (base * numBase) to this,
     * that is to prepend numBase 0 digits into digitList
     *
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
        if (other == null) {
            return this;
        }

        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        // two negative number addition = negation of addition of two absolutes
        if (this.isNegative() && other.isNegative()) {
            this.negate();
            other.negate();
            BigNumber abs = this.add(other);

            abs.negative = true;

            this.negate();
            other.negate();
            return abs;
        }

        // positive and negative
        if (this.isNegative() ^ other.isNegative()) {
            if (this.compareTo(other) > 0) {
                // this is positive, other is negative
                other.negate();

                BigNumber temp;
                if (this.compareTo(other) > 0) {
                    temp = subtract(other);
                } else {
                    temp = other.subtract(this);
                    temp.negate();
                }

                other.negate();
                return temp;
            } else {
                // this is negative, other is positive
                this.negate();

                BigNumber temp;
                if(this.compareTo(other) > 0) {
                    temp = this.subtract(other);
                    temp.negate();
                } else {
                    temp = other.subtract(this);
                }

                this.negate();
                return temp;
            }
        }

        // two positive numbers
        BigNumber result = new BigNumber(this.base);
        int carry = 0;
        Iterator<Integer> itor1 = this.digitIterator();
        Iterator<Integer> itor2 = other.digitIterator();
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
     * Absolute compare
     * @param o other object
     * @return int
     */
    @Override
    public int compareTo(BigNumber o) {
        if (o == null) {
            throw new NullPointerException("The specific object is null");
        }

        BigNumber o1 = this;

        // negative and positive
        if (o1.isNegative() ^ o.isNegative()) {
            return o1.isNegative() ? -1 : 1;
        }

        int size1 = o1.getDigitList().size();
        int size2 = o.getDigitList().size();

        if (size1 != size2) {
            return (size1 - size2);
        }

        Iterator<Integer> itor1 = o1.digitDescendingIterator();
        Iterator<Integer> itor2 = o.digitDescendingIterator();
        // comparison from most significant digit to least
        while (itor1.hasNext() && itor2.hasNext()) {
            int digit1 = itor1.next();
            int digit2 = itor2.next();
            if (digit1 != digit2) {
                return (digit1 - digit2);
            }
        }
        // otherwise equal
        return 0;
    }

    /**
     * Subtract this to other big number
     *
     * @param other big number
     * @return the difference of two big numbers, if less than 0, return 0
     */
    public BigNumber subtract(BigNumber other) {
        if (other == null) {
            return this;
        }

        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        BigNumber result = new BigNumber(this.base);

        if (this.isNegative() ^ other.isNegative()) {
            BigNumber temp;
            if (this.compareTo(other) > 0) {
                // this is positive, other is negative
                other.negate();

                temp = this.add(other);

                other.negate();
            } else {
                // this is negative, other is positive
                this.negate();

                temp = this.add(other);
                temp.negate();

                this.negate();
            }

            return temp;
        }

        if (this.isNegative() && other.isNegative()) {
            // both are negative
            this.negate();
            other.negate();

            BigNumber temp = this.subtract(other);
            temp.negate();

            this.negate();
            other.negate();

            return temp;
        }

        // two numbers are positive
        // result is 0 if this is less than or equal to other
        if (this.compareTo(other) <= 0) {
            result = other.subtract(this);
            result.negate();
            return result;
        }

        // borrow from higher digit, if current digit is less than others current digit
        int borrow = 0;
        Iterator<Integer> itor1 = this.digitIterator();
        Iterator<Integer> itor2 = other.digitIterator();
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

        result.trimTopZeros();
        return result;
    }

    /**
     * This big number multiplied with a integer
     *
     * @param other integer
     * @return big number product
     */
    public BigNumber multiply(int other) {
        if (other < 0) {
            BigNumber result = multiply(Math.abs(other));
            result.negate();
            return result;
        }

        BigNumber result = new BigNumber(this.base);

        int carry = 0;
        int digit;

        for (Iterator<Integer> itor = this.digitIterator(); itor.hasNext(); ) {
            digit = itor.next();

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
        if (carry > 0) {
            result.addDigit(carry);
        }

        return result;
    }

    /**
     * This big number multiplied with a big number
     *
     * @param other big number
     * @return big number product
     */
    public BigNumber multiply(BigNumber other) {
        if (other == null) {
            return this;
        }

        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        // a * b = - (-a) * b
        if (this.isNegative()) {
            this.negate();
            BigNumber temp = this.multiply(other);
            temp.negate();
            this.negate();
            return temp;
        }

        // a * b = - a * (-b)
        if (other.isNegative()) {
            other.negate();
            BigNumber temp = this.multiply(other);
            temp.negate();
            other.negate();
            return temp;
        }

        BigNumber result = new BigNumber(this.base);

        BigNumber left = this;
        BigNumber right = other;
        int digit2;

        // To reduce running time, the right operand should be the one with less digit list
        if (this.getDigitList().size() < other.getDigitList().size()) {
            BigNumber swap = left;
            left = right;
            right = swap;
        }

        // Resemble to the human steps of calculate the product of two number,
        // first get one digit of other operand, then multiply with this,
        // if other has more digit, multiply the previous with base
        // and add with the product of new digit and this
        Iterator<Integer> itor2 = right.digitDescendingIterator();
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

    /**
     * Power of big number
     *
     * @param exp exponent
     * @return this ^ other
     */
    public BigNumber power(BigNumber exp) {
        if (exp == null) {
            return this;
        }

        if (this.base != exp.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        BigNumber result = new BigNumber(this.base);
        result.strToNum("1");
        BigNumber base = new BigNumber(this);
        String expStr = exp.numToStr();
        StringBuffer buffer = new StringBuffer();

        // example : 2^12 = 4^6 = 16^3 = 16 * (16^2) = 16 * 256
        // this algorithm reduce the running time for power significantly
        while (!expStr.equals("0")) {
            if (isOdd(expStr)) {
                result = result.multiply(base);
            }
            // exp /= 2
            strDivideByInt(expStr, 2, buffer);
            expStr = buffer.toString();
            // square the base
            base = base.multiply(base);
            // reuse buffer
            buffer.setLength(0);
        }

        return result;
    }

    /**
     * Is this big number zero
     * In the project, this is zero iff list is empty
     * Because the end of subtract will trim out the most significant zeros
     *
     * @return whether this is zero
     */
    public boolean isZero() {
        return this.getDigitList().isEmpty();
    }

    /**
     * Helper method. Is the string representative number odd
     *
     * @param numStr string representative number
     * @return whether numStr is odd
     */
    public boolean isOdd(String numStr) {
        char least = numStr.charAt(numStr.length() - 1);
        return least == '1' || least == '3' || least == '5' || least == '7' || least == '9';
    }

    /**
     * Helper method, used to print (var, BigNumber) in the map
     *
     * @param varMap the Big Number correspondent to var name
     */
    public static void printMap(HashMap<String, BigNumber> varMap) {
        for (Map.Entry<String, BigNumber> pair : varMap.entrySet()) {
            System.out.println(pair.getKey() + " = " + pair.getValue().numToStr());
        }
    }

    /**
     * Method to parse the input and execute the semantic.
     */
    public static void executeLoop() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // store var names and their correspondent big number
        HashMap<String, BigNumber> varMap = new HashMap<String, BigNumber>();
        // store line number and expr
        ArrayList<String> exprTable = new ArrayList<String>();
        // exprTable[0] unused
        exprTable.add("");

        // read expr from stdin
        String lineExpr;
        try {
            while ((lineExpr = reader.readLine()) != null) {
                if (lineExpr.equals("")) {
                    break;
                }

                String[] params = lineExpr.split(" ");
                if (params.length != 2) {
                    System.out.println("Invalid input: wrong format");
                    return;
                }

                int lineNum = Integer.parseInt(params[0]);
                String expr = params[1];
                // set expr table for jumping
                if (exprTable.size() == lineNum) {
                    exprTable.add(expr);
                } else {
                    exprTable.set(lineNum, expr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // begin to handle expr
        int line = 1;
        while (line < exprTable.size()) {
            String expr = exprTable.get(line);

            // first handle ? jump
            if (expr.contains("?")) {
                String[] jumpParams = expr.split("\\?");
                if (jumpParams.length != 2) {
                    System.out.println("Invalid input: wrong jump");
                    return;
                }

                String var = jumpParams[0];
                int lineNum = Integer.parseInt(jumpParams[1]);

                //System.out.println(var + " = " + varMap.get(var).numToStr());
                if (!varMap.get(var).isZero()) {
                    // if var value is not 0, then go to Line number
                    line = lineNum;
                } else {
                    // else go to next line
                    line++;
                }
                continue;
            }

            //System.out.println(expr);
            // handle expr with or without = and except ?
            String[] exprParams = expr.split("=");
            int length = exprParams.length;
            if (length == 1) {
                // only var name, then print it
                System.out.println(varMap.get(exprParams[0]).numToStr());
            } else if (length == 2) {
                String left = exprParams[0];
                String right = exprParams[1];

                if (right.charAt(0) <= '9' && right.charAt(0) >= '1') {
                    // it's an assignment of number to var
                    BigNumber big = new BigNumber();
                    big.strToNum(right);
                    varMap.put(left, big);
                } else {
                    // it's a arithmetic expression
                    int k; // k point to the index of operator
                    for (k = 0; k < right.length(); k++) {
                        char token = right.charAt(k);
                        if (token == '+' || token == '-' || token == '*' || token == '/' || token == '^' || token == '%' || token == '~' || token == ')') {
                            break;
                        }
                    }

                    // operator at last char or not contained in expr
                    if (k == right.length()) {
                        System.out.println("Invalid input: wrong arithmetic expression");
                        return;
                    }

                    String leftOperand = right.substring(0, k);
                    String rightOperand = right.substring(k + 1);

                    BigNumber leftBigNumber = varMap.get(leftOperand);
                    BigNumber rightBigNumber = varMap.get(rightOperand);

                    assert leftBigNumber != null : "Invalid input: you have to initialize the variable before you use it: " + expr;

                    /*if (leftBigNumber == null && rightBigNumber == null) {
                        System.out.println("Invalid input: you have to initialize the variable before you use it");
                        return;
                    }*/

                    switch (right.charAt(k)) {
                        case '+':
                            varMap.put(left, leftBigNumber.add(rightBigNumber));
                            break;
                        case '-':
                            varMap.put(left, leftBigNumber.subtract(rightBigNumber));
                            break;
                        case '*':
                            varMap.put(left, leftBigNumber.multiply(rightBigNumber));
                            break;
                        case '/':
                            BigNumber output = new BigNumber();
                            leftBigNumber.divideByInt(Integer.valueOf(rightOperand), output);
                            varMap.put(left, output);
                            break;
                        case '^':
                            varMap.put(left, leftBigNumber.power(rightBigNumber));
                            break;
                        case '%':
                            BigNumber output2 = new BigNumber();
                            int temp = leftBigNumber.divideByInt(Integer.valueOf(rightOperand), output2);
                            BigNumber remnant = new BigNumber();
                            remnant.strToNum("" + temp);
                            varMap.put(left, remnant);
                            break;
                        case '~':
                            varMap.put(left, leftBigNumber.power(rightBigNumber));
                            break;
                        case ')':
                            varMap.put(left, leftBigNumber.power(rightBigNumber));
                            break;
                        default:
                            System.out.println("Invalid input: unknown operator");
                            return;
                    }
                }
            } else {
                System.out.println("Invalid input: unknown expression" + expr);
                return;
            }

            //System.out.println("" + line + ":");
            //printMap(varMap);
            // next line
            line++;
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            BigNumber.setSpecifiedBase(Integer.parseInt(args[0]));
            //System.out.println("Specified base = " + BigNumber.getSpecifiedBase());
        } else {
            //System.out.println("Default base = " + BigNumber.optimalBase);
        }

        executeLoop();
    }
}
