# hadoop-letter-frequency

The objective of this project is to implement a data processing pipeline that can handle substantial data sets, ensuring efficient computation and meaningful data insights. By exploiting the MapReduce paradigm, data processing tasks is split into two main functions: the ***Mapper***
and the ***Reducer***.

The Mapper function processes the input data, emitting key-value pairs, while the Reducer
aggregates these pairs to produce the final output.
The project is developed using Hadoop framework, which provides an open-source implementation of the MapReduce paradigm. Hadoop allows for the distributed processing of large data sets across clusters of computers using simple programming models. The Hadoop ecosystem also includes other tools, such as HDFS (Hadoop Distributed File System) for distributed stor-
age, and YARN (Yet Another Resource Negotiator) for cluster resource management.

## Research Topic
The objective of this project is to analyze letter frequency in text documents utilizing Hadoop’s
MapReduce framework. Specifically, two distinct approaches are adopted to optimize the
MapReduce task: the use of a Combiner and the implementation of an In-Mapper Combiner.
These methods aim to enhance the efficiency of the MapReduce process by reducing the amount
of data transferred between the Mapper and Reducer stage