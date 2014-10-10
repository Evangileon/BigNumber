/**
 * @author Jun Yu
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class jxy132330_Jun_Yu_BigNumber implements Comparable<jxy132330_Jun_Yu_BigNumber> {

    private final int base;
    private LinkedList<Integer> digitList;

    // The larger the better under restriction that the square of base will not overflow
    public static final int optimalBase = 0x7FFF;
    // the number of the integers that each list node contains
    public static final int digitsPerNode = 100;

    private static int specifiedBase = optimalBase;

    public static void setSpecifiedBase(int specifiedBase) {
        jxy132330_Jun_Yu_BigNumber.specifiedBase = specifiedBase;
    }

    public static int getSpecifiedBase() {
        return specifiedBase;
    }

    public static jxy132330_Jun_Yu_BigNumber BigNumberWithOptimalBase() {
        return new jxy132330_Jun_Yu_BigNumber(optimalBase);
    }

    public jxy132330_Jun_Yu_BigNumber() {
        this(specifiedBase);
    }

    public jxy132330_Jun_Yu_BigNumber(int base) {
        if (base > optimalBase || base <= 0) {
            base = optimalBase;
        }

        this.base = base;
        digitList = new LinkedList<Integer>();
    }

    public jxy132330_Jun_Yu_BigNumber(jxy132330_Jun_Yu_BigNumber other) {
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

    private void trimTopZeros() {
        Iterator<Integer> itor = this.getDigitList().descendingIterator();

        while (itor.hasNext()) {
            if (itor.next() == 0) {
                itor.remove();
            } else {
                return;
            }
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
     * @param intNum delimiter
     * @param buffer for output
     * @return remnant
     */
    public int divideByInt(int intNum, StringBuffer buffer) {
        String str = this.numToStr();
        return strDivideByInt(str, intNum, buffer);
    }

    /**
     * String representative big number divided by a integer
     *
     * @param numStr string representation of big number
     * @param intNum delimiter
     * @param buffer to store the string representation of the result
     * @return the remnant of the division
     */
    public int strDivideByInt(String numStr, int intNum, StringBuffer buffer) {
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
        int remnant;
        String num = str;

        while (!num.equals("0")) {
            // If num = "0", means the result of division is 0, so the loop ends
            StringBuffer buffer = new StringBuffer();
            remnant = strDivideByInt(num, this.base, buffer);
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
     * @param digit to be appended
     */
    private void addDigit(int digit) {
        if (digit < 0) {
            return;
        }
        this.digitList.add(digit);
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
    public jxy132330_Jun_Yu_BigNumber add(jxy132330_Jun_Yu_BigNumber other) {
        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        jxy132330_Jun_Yu_BigNumber result = new jxy132330_Jun_Yu_BigNumber(this.base);
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

    @Override
    public int compareTo(jxy132330_Jun_Yu_BigNumber o) {
        if(o == null) {
            return 0;
        }

        jxy132330_Jun_Yu_BigNumber o1 = this;

        int size1 = o1.getDigitList().size();
        int size2 = o.getDigitList().size();

        if(size1 != size2) {
            return (size1 - size2);
        }

        Iterator<Integer> itor1 = o1.getDigitList().descendingIterator();
        Iterator<Integer> itor2 = o.getDigitList().descendingIterator();
        // comparison from most significant digit to least
        while(itor1.hasNext() && itor2.hasNext()) {
            int digit1 = itor1.next();
            int digit2 = itor2.next();
            if(digit1 != digit2) {
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
    public jxy132330_Jun_Yu_BigNumber subtract(jxy132330_Jun_Yu_BigNumber other) {
        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        jxy132330_Jun_Yu_BigNumber result = new jxy132330_Jun_Yu_BigNumber(this.base);

        // result is 0 if this is less than or equal to other
        if(this.compareTo(other) <= 0) {
            return result;
        }

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

        result.trimTopZeros();
        return result;
    }

    /**
     * This big number multiplied with a integer
     *
     * @param other integer
     * @return big number product
     */
    public jxy132330_Jun_Yu_BigNumber multiply(int other) {
        if (other < 0) {
            return this;
        }

        jxy132330_Jun_Yu_BigNumber result = new jxy132330_Jun_Yu_BigNumber(this.base);

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
    public jxy132330_Jun_Yu_BigNumber multiply(jxy132330_Jun_Yu_BigNumber other) {
        if (this.base != other.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        jxy132330_Jun_Yu_BigNumber result = new jxy132330_Jun_Yu_BigNumber(this.base);

        jxy132330_Jun_Yu_BigNumber left = this;
        jxy132330_Jun_Yu_BigNumber right = other;
        int digit2;

        // To reduce running time, the right operand should be the one with less digit list
        if (this.getDigitList().size() < other.getDigitList().size()) {
            jxy132330_Jun_Yu_BigNumber swap = left;
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

            jxy132330_Jun_Yu_BigNumber temp = left.multiply(digit2);
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
     * @param exp exponent
     * @return this ^ other
     */
    public jxy132330_Jun_Yu_BigNumber power(jxy132330_Jun_Yu_BigNumber exp) {
        if (this.base != exp.getBase()) {
            throw new NumberFormatException("Base not the same");
        }

        jxy132330_Jun_Yu_BigNumber result = new jxy132330_Jun_Yu_BigNumber(this.base);
        result.strToNum("1");
        jxy132330_Jun_Yu_BigNumber base = new jxy132330_Jun_Yu_BigNumber(this);
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
     * @return whether this is zero
     */
    public boolean isZero() {
        return this.getDigitList().isEmpty();
    }

    /**
     * Helper method. Is the string representative number odd
     * @param numStr string representative number
     * @return whether numStr is odd
     */
    public boolean isOdd(String numStr) {
        char least = numStr.charAt(numStr.length() - 1);
        return least == '1' || least == '3' || least == '5' || least == '7' || least == '9';
    }

    /**
     * Helper method, used to print (var, jxy132330_Jun_Yu_BigNumber) in the map
     * @param varMap the Big Number correspondent to var name
     */
    public static void printMap(HashMap<String, jxy132330_Jun_Yu_BigNumber> varMap) {
        for (Map.Entry<String, jxy132330_Jun_Yu_BigNumber> pair : varMap.entrySet()) {
            System.out.println(pair.getKey() + " = " + pair.getValue().numToStr());
        }
    }

    /**
     * Method to parse the input and execute the semantic.
     */
    public static void executeLoop() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // store var names and their correspondent big number
        HashMap<String, jxy132330_Jun_Yu_BigNumber> varMap = new HashMap<String, jxy132330_Jun_Yu_BigNumber>();
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
                    jxy132330_Jun_Yu_BigNumber big = new jxy132330_Jun_Yu_BigNumber();
                    big.strToNum(right);
                    varMap.put(left, big);
                } else {
                    // it's a arithmetic expression
                    int k; // k point to the index of operator
                    for (k = 0; k < right.length(); k++) {
                        char token = right.charAt(k);
                        if (token == '+' || token == '-' || token == '*' || token == '^' || token == '%' || token == '~' || token == ')') {
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

                    jxy132330_Jun_Yu_BigNumber leftBigNumber = varMap.get(leftOperand);
                    jxy132330_Jun_Yu_BigNumber rightBigNumber = varMap.get(rightOperand);

                    if (leftBigNumber == null && rightBigNumber == null) {
                        System.out.println("Invalid input: you have to initialize the variable before you use it");
                        return;
                    }

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
                        case '^':
                            varMap.put(left, leftBigNumber.power(rightBigNumber));
                            break;
                        case '%':
                            StringBuffer buffer = new StringBuffer();
                            BigNumber temp = new BigNumber();
                            temp.strToNum("" + leftBigNumber.divideByInt(Integer.valueOf(rightOperand), buffer));
                            varMap.put(left, temp);
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
            jxy132330_Jun_Yu_BigNumber.setSpecifiedBase(Integer.parseInt(args[0]));
            //System.out.println("Specified base = " + jxy132330_Jun_Yu_BigNumber.getSpecifiedBase());
        } else {
            //System.out.println("Default base = " + jxy132330_Jun_Yu_BigNumber.optimalBase);
        }

        executeLoop();
    }
}
