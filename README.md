# Flight Times

I wrote this as a response to the OFFLFIRSOCH challenge by Solderpunk, who is most known for developing the [Gemini](https://geminiprotocol.net/) protocol.

Full details of the challenge can be found in the original post, located [here](gemini://zaibatsu.circumlunar.space/~solderpunk/gemlog/announcing-offlfirsoch-2024.gmi) on gemini, or [here](https://geminiproxy.p.projectsegfau.lt/gemini/zaibatsu.circumlunar.space/~solderpunk/gemlog/announcing-offlfirsoch-2024.gmi) in proxied format.

The OFFLFIRSOCH, which expands to OFFLine-FIRst SOftware CHallenge, asked people to spend the month of March developing and sharing a piece of software which is offline-first:

> By this I mean it either makes no use of the internet whatsoever and happily functions entirely offline without even being aware that anything "unusual" is happening, or else it does make use of the internet but is fundamentally designed around the assumption that the internet is not always available and maintains 90% or more of its functionality when used offline, and will continue to do so no matter whether it gets internet access once per week, once per month, once per year or even longer.

I chose to write a cli utility for looking up **information about flight times and distances.** There are many online calculators for this sort of thing, but as Solderpunk pointed out, the spirit of lies also in replicating this kinds of tools that are abundant on the web, but which are entirely useless when there is no internet connection.

The project relies heavily on the following datasource:

- [Global Airport Database](https://www.partow.net/miscellaneous/airportdatabase/)

## Installation

This software runs using `babashka`, a version of `clojure` that uses the `GraalVM`. You will need to have `babashka` installed to run it.

Luckily, babashka is very easy to install with one line. [Details can be found here](https://github.com/babashka/babashka?tab=readme-ov-file#quickstart)

To run the program, first clone this repo:

``` sh

git clone [repo-name]

```

Then, just run `bb` followed by the name of the main file and the relevant arguments (detailed below).

``` sh
bb airports 'New York' 'Paris'
```

## Usage



