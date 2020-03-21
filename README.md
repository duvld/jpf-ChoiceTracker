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
your .jpf or config file. Usage: choice.<action>=<option>

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
