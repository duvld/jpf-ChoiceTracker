
# jpf-ChoiceTracker
An updated and improved version of the ChoiceTracker listener for JPF (JavaPathFinder). 

# What this listener does:
This listener is a generic choice tracker tool. When JPF finishes it produces
a list of choice values that can be used to create readable replay scripts
etc.

How to use this class: This listener only displays output if
'propertyVioalted' occurs, i.e., in your app, the ChoiceTracker tracks the
choices made leading to an exception.

There are some customization options available for the user, add these to
your .jpf or config file. Usage: `choice.<action>=<option>`

* choice.excludes 
  * provide a comma separated list of types to exclude from the
tracker

* choice.class 
  * provide a comma separated list of types to keep track of
(leave empty for all types)

* choice.format 
  * choose between formats CG and CHOICE

* choice.show_location 
  * proivde a boolean to toggle on or off, shows location
of choices, default is true

# Example
A basic demonstration of how to run this listener

**Consider the following app**
```
public class Example {
	public static void main(String[] args) {
		Random random = new Random();
		if (random.nextDouble() > 0) {
			throw new RuntimeException();
		} else {
			if (random.nextInt(2) == 0) {
				throw new RuntimeException();
			}
		}
	}
}
```
If we run the above with the following JPF configuration:
```
target=Example                                                                
classpath=/home/anjitong/Dev/Eclipse/workspace/4315/bin                         
listener=ChoiceTracker                                                          
native_classpath=/home/anjitong/Dev/Eclipse/workspace/4315/bin                  
cg.enumerate_random=true                                                        
choice.trace=  
```
JPF would produce the following output:
```
====================================================== system under test
Example.main()

====================================================== search started: 18/04/20 4:22 PM
0
2
// application: Example.main()
// trace over all CG classes
//------------------------- choice trace
   0: ThreadChoiceFromSet {id:"ROOT" ,1/1,isCascaded:false}
 	at java.lang.Boolean.[<clinit>](Boolean.java:0)
   1: DoubleChoiceFromList[id="verifyDoubleList([D)",isCascaded:false,>4.9E-324,0.0,1.7976931348623157E308]
 	at java.util.Random.nextDouble(Random.java)
//------------------------- end choice trace

====================================================== error 1
gov.nasa.jpf.vm.NoUncaughtExceptionsProperty
java.lang.RuntimeException
	at Traversal.main(Traversal.java:9)


====================================================== snapshot #1
thread java.lang.Thread:{id:0,name:main,status:RUNNING,priority:5,isDaemon:false,lockCount:0,suspendCount:0}
  call stack:
	at Traversal.main(Traversal.java:9)


====================================================== results
error #1: gov.nasa.jpf.vm.NoUncaughtExceptionsProperty "java.lang.RuntimeException  at Example.main(Trav..."

====================================================== statistics
elapsed time:       00:00:00
states:             new=2,visited=0,backtracked=0,end=0
search:             maxDepth=2,constraints=0
choice generators:  thread=1 (signal=0,lock=1,sharedRef=0,threadApi=0,reschedule=0), data=1
heap:               new=374,released=2,maxLive=0,gcCycles=1
instructions:       3219
max memory:         240MB
loaded code:        classes=66,methods=1377

====================================================== search finished: 18/04/20 4:22 PM
```
