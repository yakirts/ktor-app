# Iguzio Home Assignment

 

The program should accept four parameters:

 

* The location of one input file

* The location of second output file

* The URLs for 2 HTTP endpoints

 

The input file can be of any length, but its lines are guaranteed to be reasonably short (let's say up to 128 characters), and separated by a newline character. For example:

Raising

Skinny

Elephants

Is

Utterly

Boring

 

For each line in the input file, you are required to send its content to both HTTP endpoints. Each endpoint will respond with its own string, which is also guaranteed to be reasonably short (let's say up to 128 characters).

If the strings match, you need to write a line with the string "true" to the output file. If they don't, write "false". The lines in the output file must match the lines in the input file!

So, if the 5th line in the input file got the same response from both endpoints, then the 5th line in the output file must read "true".

 

## Running it

Run App.kt , assign (inputFilePath,outputFilePath,endPoint1,endPoint2)



