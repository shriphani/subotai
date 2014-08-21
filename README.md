# subotai

<img src="subotai.jpg" align="right" />
> Subotai died at age 73, by which time he had conquered 32 nations and won 65 pitched battles, 
> as the Muslim historians tell us. For 60 of those years, Subotai lived as Mongol soldier, 
> first as a lowly private who kept the tent door of Genghis himself, 
> rising to be the most brilliant and trusted of Genghis Khan's generals. 
> When Genghis died, Subotai continued to be the moving force of the Mongol 
> army under his successors. It was Subotai who planned and participated in
> the Mongol victories against Korea, China, Persia, and Russia. It was Subotai's
> conquest of Hungary that destroyed every major army between the 
> Mongols and the threshold of Europe.
> - from Subotai the Valiant: Genghis Khan's Greatest General by Richard A. Gabriel

Subotai contains a swiss-army-knife of data-mining tools on HTML
documents. It contains routines for:

1. Comparing the similarity (in structure) of HTML documents.
2. Testing if two documents are near-duplicates in a way that scales
to large web corpora.

## Literature

A list of the algorithms implemented:

* Structural similarity using tree-edit-distance from [Reis,
  Davi de Castro, et al](doc/rtdm.pdf), and a version I hacked
  together from a previous project that uses a vector-space
  representation and cosine similarity.
* Near-Duplicate Detection (a naive algorithm from the IRBook by
  Manning et al and a scaleable version from [Manku et al](doc/ndd.pdf)).


## Usage

For using this with leiningen:
```clojure
[subotai "0.2.16"]
```

With maven:
```xml
<dependency>
  <groupId>subotai</groupId>
  <artifactId>subotai</artifactId>
  <version>0.2.16</version>
</dependency>
```

### Structural Similarity

Structural similarity routines are available in the
<code>subotai.structural-similarity</code> namespace.

To check if two documents have the same underlying structure (for
example, different pages of the same blog):

```clojure
user=> (use 'clj-http.client)
user=> (use 'subotai.structural-similarity.core :reload)
user=> (def bod1 (:body (get "http://blog.shriphani.com/"))) ; this is page 1
#'user/bod1
user=> (def bod2 (:body (get "http://blog.shriphani.com/index-2.html"))) ; this is page 2
#'user/bod2
user=> (similar? bod1 bod2)
true ; both pages have the same structure
user=>
```

The two pages in the above example look like:
<table>
    <tr>
    	<td><img src="blog_page_1.png" style="display:inline" /></td>
	    <td><img src="blog_page_2.png" style="display:inline"/></td>
	</tr>
</table>

### Near Duplicate Detection

Near Duplicate Detection routines are available in the
<code>subotai.near-duplicate</code> namespace. The simplest algorithm
implemented is the shingles algorithm (build a list of 4-grams,
compute the jaccard similarity, and perform a threshold test). I also
have a more scaleable algorithm from Manku et al (from WWW '07). To
compare two HTML documents (we default to the scaleable version but
you can specify a different algorithm in the function call).

For example, here are 2 documents (two documents that contain the same
content but are arrived at via different links like it often happens
when you are crawling web-pages).

```clojure
user> (use 'subotai.near-duplicate.utils)
nil
user> (near-duplicate-html? bod-1 bod-2)
true
user> (use 'subotai.near-duplicate.core :reload)
nil
user> (def bod-1 (:body (get "http://www.kidneyspace.com/index.php/topic,5304.0.html"))) ; this is the first page
#'user/bod-1
user> (def bod-2 (:body (get "http://www.kidneyspace.com/index.php/topic,5304.msg30671.html"))) ; this is the second page
#'user/bod-2
user> (near-duplicate-html? bod-1 bod-2)
true ; and they are near duplicate
user> 
```

Also, you can specify the algorithm you want to use (shingles or
fingerprint) depending on your choice.
```clojure
user> (near-duplicate-html? bod-1 bod-2 :shingles)
true
user> (near-duplicate-html? bod-1 bod-2 :fingerprint)
true
user> 
```

### Reading WARC files

Warc files are the standard file format used for archiving large HTML
corpora. Several of the largest web corpora (ClueWeb09, ClueWeb12, and
The Common Crawl) are shipped as a collection of warc files.

An example routine would be:

```clojure
(use 'subotai.warc.warc)

(defn usage-example
  []
  (with-open [instream (warc-input-stream "/Users/shriphani/Documents/warc-clojure/0000wb-00.warc.gz")]
    (doall
     (map
      (fn [r]
        (-> r :warc-target-uri))
      (stream-warc-records-seq instream)))))

(take 3 (usage-example))
```

Which returns:

```clojure
(nil "http://ahmetertug.com/ahmetertug.html" "http://ahmetertug.com/contactus.html")
```

A single record in a Warc file contains of some metadata stored in the
header and a payload. An example record is:

```clojure
{:payload ....
 :warc-type "response",
 :warc-date "2011-02-18T23:32:56Z",
 :content-length "4928",
 :warc-record-id "<urn:uuid:00127f49-b6d8-413e-857b-5a7620368f88>",
 :warc-ip-address "125.7.5.24",
 :warc-payload-digest "sha1:M4VJCCJQJKPACSSSBHURM572HSDQHO2P",
 :warc-target-uri
 "http://whitiangamarine.tradeaboat.co.nz/emailAFriend.aspx?item=H4sIAGW4X00A%2fwFwAo%2f9gaXg6UTMkoLWV1Zy9nOhybsaOj36okTTM%2fCdGlV9et4wGW8ywbKoacCcFSjvDmf7BgE%2bke8eDGs5H4ib0RuE96Yj2%2fR5LIXmy1SUEue5IiHmYmS9jl9femiZGo6yAeW0fX%2bSnCkd5D%2bOW5216i0SJ9yb0PZJ%2fI%2f3z3manNAv042wJYFyUgOGpN6yV2wZGUEERk5FQI%2bmSASd88RTsytzksZuC%2fmTpDowhevXiY3N2%2br1n6Q9utfvEKuy5bonZPqy7BlK93yJ9DnviiT0ZJMsHGOTXC0NUywIonFpIXfogmm8y6I3RfXxQXD5p95qmiogdI1rvPgKCaV%2bgO4nZ4r%2fCAicl697pcwFKCQyFW5ZTS74%2bSnrdEssBdz2quceotYDcW2GH3hogkrRupiqN9hFdVsb2p3HXP%2fYGkH9W6%2bD8jp7TyLmALvnJJevST%2f6wlbQRhWrsNlPXnTjxQZrTw7z8E%2f%2bo5BFsb6HgWfXzULQZ2RnNFvAZOMgkcKtHopRTbA6cp5ifB8j8sFoV7PVwifNgcLBR28EKMjAeBqRZnBlB4nJwEISomyeNIBP%2fQlvpV4sqArZdUhs1qRi9TOQ%2fToiaSrlKpq%2bSdSbuZqjXIJ9b%2ftjgx8biQe129TDOB0BDHtEXwqq1aoaASxmTqddrYKqCRvcKjfH1aYSZHyL9p6xS6LwMAlO2myGxnZeGkrVpfr5C%2fEDJp6HR%2f28EgR4fdXyyRWauMhoPrQgXYJTq7NQwv7m8JYyvxCfGpX6Kz6ftu4NMBAHPuhGxd%2fEDDP5y3DUIcJBCAyMMvvMOJQXMXb8cpsyTv9ZcU1RN5ehrp2iyPudY%2b6iHHACAAA%3d",
 :content-type "application/http; msgtype=response"}
```

## License

Copyright © 2014 Shriphani Palakodety

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
