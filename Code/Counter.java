
/**
 * A counter that will roll over to the min
 * value when attempting to increase it beyond the maximum value and to the max 
 * value when attempting to decrease it below the min value
 * 
 * @author Charles Hoot 
 * @version 4.0
 */
public class Counter
{
    // PUT PRIVATE DATA FIELDS HERE
    private int min;
    private int max;
    private int value=0;
    private boolean rolledOver=false;

    /**
     * The default constructor for objects of class Counter.  Minimum is 0 and the maximum
     * is the largest possible integer.
     */
    public Counter()
    {
        this.min=0;
        this.max=Integer.MAX_VALUE;
        this.value=this.min;
        this.rolledOver=false;
    }
    
    
    /**
     * The alternate constructor for objects of class Counter.  The minimum and maximum values are given as parameters.
     * The counter starts at the minimum value.
     * @param min The minimum value that the counter can have
     * @param max The maximum value that the counter can have
     * */
    public Counter(int min, int max)
    {
        if (min>max || min==max){
            throw new CounterInitializationException("Minimum value cannot be greater than or equal to maximum value");
        }
        else{
            this.min=min;
            this.max=max;
            this.value=min;
            this.rolledOver=false;
        }
        
        
    }
    

    /**
     * Determine if two counters are in the same state
     *
     * @param  otherObject   the object to test against for equality
     * @return     true if the objects are in the same state
     */
    @Override
    public boolean equals(Object otherObject)
    {
        boolean result = false;
        if (otherObject instanceof Counter)
        {
            Counter other = (Counter) otherObject;
            // YOUR CODE GOES HERE
            if (this.min==other.getMin() && this.max==other.getMax() && this.value==other.value() && this.rolledOver==other.rolledOver){
                result=true;

            }
            
        }
        return result;
    }
    
    public int getMin(){
        return this.min;
    }
    public int getMax(){
        return this.max;
    }

    /**
     * Increases the counter by one
     */
    public void increase()
    {
        this.rolledOver = (this.value == this.max);
        if (this.rolledOver){
            this.value = this.min;
        }
        else{
            ++this.value;
        }
    }
 
 
     /**
     * Decreases the counter by one
     */
    public void decrease()
    {
        this.rolledOver = (this.value == this.min);
        if (this.rolledOver){
            this.value = this.max;
        }
        else{
            --this.value;
        }
    }
    
    /**
     * Get the value of the counter
     *
     * @return     the current value of the counter
     */
    public int value()
    {
        // CHANGE THE RETURN TO GIVE THE CURRENT VALUE OF THE COUNTER
        return this.value;
		
    }
    
    
    /**
     * Accessor that allows the client to determine if the counter
     *             rolled over on the last count
     *
     * @return     true if the counter rolled over
     */
    public boolean rolledOver()
    {
        return this.rolledOver;
    }
    
    /**
     * Override the toString method to provide a more informative
     * description of the counter
     *
     * @return     a descriptive string about the object
     */
    public String toString()
    {
        // CHANGE THE RETURN TO A DESCRIPTION OF THE COUNTER
        // MUST FOLLOW THE FOLLOWING FORMAT:
        //Counter: value=1 min=1 max=9 rolled over=false

        return "Counter: value=" + this.value + " min=" + this.min + " max=" + this.max + " rolled over=" + this.rolledOver;		
    }
 
}
