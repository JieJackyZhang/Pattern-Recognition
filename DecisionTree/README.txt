Decision Tree Induction by ID3, C4.5 and CART Algorithms

*********************************
Example in textbook
*********************************
Welcome to Classification: Decision Tree Induction >>>>>>

[1] Information Gain (ID3)
[2] Gain Ratio (C4.5)
[3] Gini Index (CART)

Please choose the ALGORITHM: 1
Please enter the TRAINING file name: in/data_textbook.txt
Please enter the TEST file name: in/test_textbook.txt
Please enter the OUTPUT file name: out/out.txt

Read training data >>>	Complete!
DECISION TREE
   age->
     senior
       credit_rating->
         excellent
             =no
         fair
             =yes
     middle_aged
         =yes
     youth
       student->
         no
             =no
         yes
             =yes
Training time: 21ms