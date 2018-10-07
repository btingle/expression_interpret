package csc.pkg210.lab;
import java.util.*;
import java.lang.Math;
/**
 *
 * @author Ben Tingle
 * 
 * So this isn't exactly what you asked for.
 */
public class project1 {
    
    private static char func_table(String func) // turns functions into single characters so I don't have to deal with a bunch of string objects
    {
        switch(func)
        {
            case "sin": return 's';
            case "cos": return 'c';
            case "tan": return 't';
            case "log": return 'l';
            default   : return '!'; // default char value for unknown functions
        }
    }
    
    private static int get_priority(char op) // priority corresponds to order of operations
    {
        switch(op)
        {
            case ')': return 0;
            case '+': return 1;
            case '-': return 1;
            case '*': return 2;
            case '/': return 2;
            case '^': return 3; // power operator, ex; 2^3 = 8
            case 's': return 4;
            case 'c': return 4;
            case 't': return 4;
            case 'l': return 4;
            case '(': return 5;
            default : return -1;
        }
    }
    
    private static String join(String[] split_text) // apparently there is no native JDK method for this. Why
    {
        String joined_string = "";
        for(String str:split_text)
        {
            joined_string += str;
        }
        return joined_string;
    }
    
    ///SHUNTING YARD ALGORITHM
    /*
     * This bit of code uses what is called the "shunting yard" algorithm to parse the raw expression text into something a little more machine friendly
     * This is *my* implementation of the algorithm. No code was copy pasted or stolen in the making of this.
     * Shunting yard is pretty simple - you have three buffers. One is the raw text (whitespace removed), another is the operator queue, and the last is the results bin
     * Raw text is read through and sorted into these buffers. If a number is encountered, it is always thrown into the results bin.
     * When an operator is encountered, it is thrown into the operator queue. Before it enters, if there are any operators with a higher or equal "priority" than it in the queue, they are ejected into the results bin
     * This priority corresponds to order of operations, a la PEMDAS. I've added in a couple extra operations outside of the basic arithmetical ones, unary operators like sin and cos, as well as the ^ power operator
     * Parentheses introduce the only strangeness to this. A closing ) parentheses has a very low priority, so it flushes out the operator queue.
     * The problem is, we only want it to be flushed out until the starting ( parentheses, so we make it so that when a starting parentheses is encountered, flushing of the queue ends and the parentheses are discarded
     * Once all text has been run through, any remaining operators are put into the results bin.
     */
    private static List<String> shunt(String raw_text)
    {
        List<Character> ops_queue = new LinkedList<>();  // the actual type is called "char", why do I have to put "Character" in to the pointy brackets??
        List<String> expression_text = new ArrayList<>(); // buffer for actual text of expression
        
        String num_buffer = ""; // holds multi-digit ints/floats
        String fnc_buffer = ""; // holds function names
        
        char curr_op;
        int  curr_prio = 0;
        
        char[] refined_text = join(raw_text.split(" ")).toCharArray(); // removes spaces from raw string, converts to more useful char array
        
        for(int j = 0; j < refined_text.length; j++)
        {
            curr_op = refined_text[j];
            
            if(Character.isDigit(curr_op) || curr_op == '.')
            {
                if(!fnc_buffer.equals("")) // If a number is encountered when characters are still being processed, something is wrong. Ex: sin9, cos0, etc..
                {
                    System.out.println("Error: Unexpected number");
                    return null;
                }
                num_buffer += curr_op;
                continue; // always go to next iteration on alphanumeric character
            }
            if(Character.isAlphabetic(curr_op))
            {
                if(!num_buffer.equals("")) // If a character is encountered when numbers are still being processed, something is wrong. Ex: 100b, 9x, etc..
                {
                    System.out.println("Error: Unexpected character");
                    return null;
                }
                fnc_buffer += curr_op;
                continue; // always go to next iteration on alphanumeric character
            }
            if(!num_buffer.equals("")) // if there is a number in the number buffer, add it to the results bin
            {
                expression_text.add(num_buffer);
                num_buffer = "";
            }
            if(!fnc_buffer.equals("")) // if there is a string in the function name buffer, convert it and add it to the operator table
            {
                curr_op = func_table(fnc_buffer); // checks if function name in buffer is in the list of valid function values
                if(curr_op == '!')
                {
                    System.out.println("Error: Uknown Function");
                    return null;
                }
                
                fnc_buffer = "";
                j--; // if a function was just completed, process it and redo the current operator next round
            }
            if(get_priority(curr_op) >= 0) // sees if this is a valid operator
            {
                curr_prio = get_priority(curr_op);
            
                for(int i = 0; i < ops_queue.size(); i++)
                {
                    char op = ops_queue.get(i);
                    if(op == '(') // if operator in the queue is a starting bracket, stop everything and go to the next loop
                    {
                        ops_queue.remove(i);
                        break;
                    }
                    else if(get_priority(op) >= curr_prio) // if operator in the queue is lower priority than the current op, move it into the results bin
                    {
                        expression_text.add(String.valueOf(op));
                        ops_queue.remove(i);
                        i--; // need to do this after removing something from list bc indices change
                    }
                }

                if(curr_op != ')') // ')' basically just flushes out all ops, so it shouldn't really be in the queue
                {
                    ops_queue.add(0, curr_op); // moves the current op into the operator queue
                }
            }
            else
            {
                System.out.println("Error: Unknown/Invalid Operator");
                return null;
            }
        }
        
        if(!num_buffer.equals(""))
        {
            expression_text.add(num_buffer);
        }
        
        for(char op: ops_queue)
        {
            expression_text.add(String.valueOf(op));
        }
        
        if(expression_text.size() > 0) // the only situation in which this would be false is if a function, known or unknown, was entered without anything else before or after it, e.g: "sin" or "asdf" or "cos"
        {
            return expression_text;
        }
    
        System.out.println("Error: Invalid Function");
        return null;
    }
    
    private static double pow(double a, double b) // as requested, here it is
    {
        double tmp = 1;
        for(int i = 0; i < (int)b; i++) // If a decimal is entered for b, floors it and moves on
        {
            tmp *= a;
        }
        return tmp;
    }
    
    ///INTERPRET ALGORITHM
    /*
     * Interpret takes in the output of shunt and processes it into an output value.
     * Interpret has a FILO (first in, last out) queue of registers, like the operator queue in shunt, that it stores numbers in.
     * When an operator is encountered, it will take the last N numbers (N being the amount of arguments the operator takes in) in the register queue, process them, and add the result to the queue
     * If the expression is valid, the result of the expression will be the first and only number in the register queue at the end, which is then printed to screen.
     */
    private static void interpret(List<String> expression_text)
    {
        List<Double> registers = new LinkedList<>();
        
        for(String elem:expression_text)
        {
            if(Character.isDigit(elem.charAt(0)))
            {
                registers.add(0, Double.valueOf(elem));
            }
            else
            {
                char curr_op = elem.charAt(0); // if the element isn't numerical, then it should just be one character long
                double tmp = 0;
                
                if(registers.size() > 1) // Checks if there are enough values in register for 2-value operator
                {
                    switch(curr_op)
                    {
                        case '+': tmp = registers.get(1) + registers.get(0);
                                  registers.remove(0); registers.remove(0);
                                  registers.add(0, tmp);continue;               // Once result has been obtained for this operator, go to the next loop, there is nothing else that should be done
                        case '-': tmp = registers.get(1) - registers.get(0);
                                  registers.remove(0); registers.remove(0);
                                  registers.add(0, tmp);continue;
                        case '*': tmp = registers.get(1) * registers.get(0);
                                  registers.remove(0); registers.remove(0);
                                  registers.add(0, tmp);continue;
                        case '/': tmp = registers.get(1) / registers.get(0);
                                  registers.remove(0); registers.remove(0);
                                  registers.add(0, tmp);continue;
                        case '^': tmp = pow(registers.get(1) , registers.get(0));
                                  registers.remove(0); registers.remove(0);
                                  registers.add(0, tmp);continue;
                    }
                }
                if(registers.size() > 0)
                {
                    switch(curr_op)
                    {
                        case 's': tmp = Math.sin(Math.toRadians(registers.get(0)));
                                  registers.remove(0);
                                  registers.add(0, tmp);continue;
                        case 'c': tmp = Math.cos(Math.toRadians(registers.get(0)));
                                  registers.remove(0);
                                  registers.add(0, tmp);continue;
                        case 't': tmp = Math.tan(Math.toRadians(registers.get(0)));
                                  registers.remove(0);
                                  registers.add(0, tmp);continue;
                        case 'l': tmp = Math.log(registers.get(0));
                                  registers.remove(0);
                                  registers.add(0, tmp);continue;
                        default : System.out.println("Error: Invalid Expression"); // If there is only one value in the register and the operation is none of these, return an error, as something is wrong
                                  registers.clear();return;                        // Likely example: 5 ** 7, or 9 ++ 2
                    }
                }
                else
                {
                    System.out.println("Error: Invalid Expression"); // will only happen if there are no numbers before operators in the expression
                    return;
                }
            }
        }

        System.out.println(registers.get(0));
        
    }
    
    public static void interp_expression(String expression)
    {
        List<String> temp = shunt(expression);
        if(temp != null)
        {
            interpret(temp);
        }
    }
    
    public static void main(String[] args)
    {
        
        Scanner input_scanner = new Scanner(System.in);
        String  input = "";
        
        System.out.println("Type a mathematical expression. Trig functions are in degrees mode. Type \"quit\" to exit, or \"help\" for help.");
        while(!input.equals("quit"))
        {
            System.out.print(">> ");
            input = input_scanner.nextLine();
            if(!input.equals("quit") && !input.equals("help"))
            {
                interp_expression(input);
            }
            if(input.equals("help"))
            {
                System.out.println("Supported Operators:");
                System.out.println("+, -, *, /, ^, sin, cos, tan, log");
            }
        }
        
    }
}
