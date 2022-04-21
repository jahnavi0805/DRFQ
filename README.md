# DRFQ

This is the implementation of memoryless DRFQ and Dovetailing DRFQ

To compile and run the program :
javac drfq.java
java drfq "input CSV File" ["Type"]

You can enter an csv file with the following format:
1st column contains flow ids of the packets
2nd column contains arrival times of the packets
3rd column contains requirements of the packets for each resource separted by semi-colon(;)

Type of the file can be memoryless or DoveTailing and it is optional
By default the program considers memoryless DRFQ

Reference:https://people.eecs.berkeley.edu/~alig/papers/drfq.pdf


