# Flights

I wrote this as a response to the OFFLFIRSOCH challenge by Solderpunk, who is most known for developing the [Gemini](https://geminiprotocol.net/) protocol.

Full details of the challenge can be found in the original post, located on gemini at gemini://zaibatsu.circumlunar.space/~solderpunk/gemlog/announcing-offlfirsoch-2024.gmi , or [here](https://geminiproxy.p.projectsegfau.lt/gemini/zaibatsu.circumlunar.space/~solderpunk/gemlog/announcing-offlfirsoch-2024.gmi) in proxied format.

The OFFLFIRSOCH challenge, which expands to OFFLine-FIRst SOftware CHallenge, asked people to spend the month of March developing and sharing a piece of software which is offline-first:

> By this I mean it either makes no use of the internet whatsoever and happily functions entirely offline without even being aware that anything "unusual" is happening, or else it does make use of the internet but is fundamentally designed around the assumption that the internet is not always available and maintains 90% or more of its functionality when used offline, and will continue to do so no matter whether it gets internet access once per week, once per month, once per year or even longer.

I chose to write a cli utility for looking up **information about flight times and distances.** There are many online calculators for this sort of thing, but as Solderpunk pointed out, the spirit of the challenge lies also in replicating this kinds of tools that are abundant on the web, but which are entirely useless when there is no internet connection.

The project relies heavily on the following datasource:

- [Global Airport Database](https://www.partow.net/miscellaneous/airportdatabase/)

## Installation

This software runs using `babashka`, a version of `clojure` that uses the `GraalVM`. You will need to have `babashka` installed to run it.

Luckily, babashka is very easy to install with one line. [Details can be found here](https://github.com/babashka/babashka?tab=readme-ov-file#quickstart)

Once babashka is installed, to run the program first clone this repo:

``` sh

git clone [repo-name]

```

Then, `cd` into the repo and  just run `bb` followed by the name of the main file and the relevant arguments (detailed below).

``` sh
bb airports 'New York' 'Paris'
```

## Usage

You can either use the script in a simple way by providing two arguments (and origin and destination), or with a few additional options.

### Flight Information
For example, the following query:

``` sh
bb flights 'Berlin' 'Madrid'
```

Returns the following text to the terminal:

``` text
------------------------------------------------
Origin: Tegel Airport in Berlin, Germany
Destination: Getafe Airport in Madrid, Spain

Approximate Distance: 1877 km

Estimated Flight Time: Between 2 hours, 35 minutes and 3 hours, 2 minutes

Approximately 244 kg of CO2 emitted, which is around 3% of a EU person's annual average emissions and -355kg below the necessary average annual emissions recommended to stop climate change.
------------------------------------------------
```

At times, the queries may return unexpected answers. Feel free to be a bit more specific, for example:

``` sh
bb flights 'Berlin, Germany' 'John F Kennedy, New York'
```


### Searching for City and Airport names

If you want to search for the names of specific airports within a city, you can use the `-a` flag (for 'airports'):

``` sh
bb flights -a 'Berlin'
```

Similarly, if you want to search for all the cities within a country, you can use the `-c` flat (for 'cities'):

``` sh
bb flights -c 'Germany'
```

### Returning data

If you would like to use the functionality of this program within another pipeline, you can also request for it to return the response in either `json` or `edn` format by using the `-d` flag. For example:

``` sh
bb flights -d :json 'Warsaw' 'Dublin' | jq "."
```

## Adapting and Building
