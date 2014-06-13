# subotai

<img src="subotai.jpg" /><br />

Subotai contains a swiss-army-knife of data-mining tools on HTML
documents. It contains routines for:

1. Comparing the similarity (in structure) of HTML documents.
2. Extracting sets of records from these HTML documents.

## Literature

A list of the algorithms implemented:

* Structural similarity using tree-edit-distance from [Reis,
  Davi de Castro, et al](doc/rtdm.pdf)
* Extract records from a web-page.


## Usage

Structural similarity routines are available in the
<code>subotai.structural-similarity</code> namespace. Record
extraction routines are in the <code>subotai.records</code> namespace.

To check if two documents have the same underlying structure, you can
use subotai like so:



## License

Copyright © 2014 Shriphani Palakodety

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
