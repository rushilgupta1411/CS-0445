/**
 * A class that represents a rational number in normal form where the numerator
 * and the denominator share no common factors and only the numerator
 * can be negative.
 * 
 * @author Charles Hoot 
 * @version 4.0
*/

public class Rational
{
    private int numerator;
    private int denominator;
    /**
     * The default constructor for objects of class Rational.  Creates the rational number 1.
     */
    public Rational()
    {       
        this.numerator=1;
        this.denominator=1;
    }

    /**
     * The alternate constructor for objects of class Rational.  Creates a rational number equivalent to n/d.
     * @param n The numerator of the rational number.
     * @param d The denominator of the rational number.
     */    
    public Rational(int n, int d)
    {
        if (d==0){
            throw new ZeroDenominatorException("Denominator cannot be zero");
        }
        else{
            this.numerator=n;
            this.denominator=d;
            this.normalize();
        }
    }
    
    /**
     * Get the value of the Numerator
     *
     * @return     the value of the numerator
     */
    public int getNumerator()
    {
        return this.numerator;
    }
    
    /**
     * Get the value of the Denominator
     *
     * @return     the value of the denominator
     */
    public int getDenominator()
    {
        return this.denominator;
    }


    /**
     * Negate a rational number r
     * 
     * @return a new rational number that is negation of this number -r
     */    
    public Rational negate()
    {               
        return new Rational(-this.numerator, this.denominator);
    }


    /**
     * Invert a rational number r 
     * 
     * @return a new rational number that is 1/r.
     */    
    public Rational invert()
    {               
        return new Rational(this.denominator, this.numerator);
    }





    /**
     * Add two rational numbers
     *
     * @param other the second argument of the add
     * @return a new rational number that is the sum of this and the other rational
     */    
    public Rational add(Rational other)
    {       
        return new Rational(this.numerator * other.denominator
                            + other.numerator * this.denominator,
                            this.denominator * other.denominator);
    }
    
     /**
     * Subtract a rational number t from this one r
     *
     * @param other the second argument of subtract
     * @return a new rational number that is r-t
     */    
    public Rational subtract(Rational other)
    {               
        return new Rational(this.numerator * other.denominator
                            - other.numerator * this.denominator,
                            this.denominator * other.denominator);
    }

    /**
     * Multiply two rational numbers
     *
     * @param other the second argument of multiply
     * @return a new rational number that is the product of this object and the other rational.
     */    
    public Rational multiply(Rational other)
    {       
        return new Rational(this.numerator * other.numerator,
                            this.denominator * other.denominator);
    }
        
 
     /**
     * Divide this rational number r by another one t
     *
     * @param other the second argument of divide
     * @return a new rational number that is r/t
     */    
    public Rational divide(Rational other)
    {               
        return new Rational(this.numerator * other.denominator,
                            this.denominator * other.numerator);
    }
     
      
 /**
     * Put the rational number in normal form where the numerator
     * and the denominator share no common factors.  Guarantee that only the numerator
     * can be negative.
     *
     */
    private void normalize()
    {
        int g=this.gcd(Math.abs(this.numerator), Math.abs(this.denominator));
        this.numerator /= g;
        this.denominator /= g;
        if (this.denominator < 0){
            this.numerator = -this.numerator;
            this.denominator = -this.denominator;
        }
    }
    
    /**
     * Recursively compute the greatest common divisor of two *non-negative* integers
     *
     * @param a the first argument of gcd
     * @param b the second argument of gcd
     * @return the gcd of the two arguments
     */
    private int gcd(int a, int b)
    {
        int result = 0;
        if(a<b)
            result = gcd(b,a);
        else if(b==0)
            result = a;
        else
        {
            int remainder = a % b;
            result = gcd(b, remainder);
        }
        return result;
    }
   
    
    
}
