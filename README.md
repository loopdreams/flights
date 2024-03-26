# Flights

I wrote this as a response to the OFFLFIRSOCH challenge by Solderpunk, who is most known for developing the [Gemini](https://geminiprotocol.net/) protocol.

Full details of the challenge can be found in the original post, located on gemini at gemini://zaibatsu.circumlunar.space/~solderpunk/gemlog/announcing-offlfirsoch-2024.gmi , or [here](https://geminiproxy.p.projectsegfau.lt/gemini/zaibatsu.circumlunar.space/~solderpunk/gemlog/announcing-offlfirsoch-2024.gmi) in proxied format.

The OFFLFIRSOCH challenge, which expands to OFFLine-FIRst SOftware CHallenge, asked people to spend the month of March developing and sharing a piece of software which is offline-first:

> By this I mean it either makes no use of the internet whatsoever and happily functions entirely offline without even being aware that anything "unusual" is happening, or else it does make use of the internet but is fundamentally designed around the assumption that the internet is not always available and maintains 90% or more of its functionality when used offline, and will continue to do so no matter whether it gets internet access once per week, once per month, once per year or even longer.

I chose to write a cli utility for looking up **information about flight times and distances.** There are many online calculators for this sort of thing, but as Solderpunk pointed out, the spirit of the challenge lies also in replicating the kinds of tools that are abundant on the web, but which are entirely useless when there is no internet connection.

The project relies heavily on the following data source:

- [Global Airport Database](https://www.partow.net/miscellaneous/airportdatabase/)

## Installation

This software runs using `babashka`, a version of `clojure` that uses the `GraalVM`. You will need to have `babashka` installed to run it.

Luckily, babashka is very easy to install with one line. [Details on how to install babashka can be found here](https://github.com/babashka/babashka?tab=readme-ov-file#quickstart)

Once babashka is installed, clone this repo:

``` sh

git clone https://github.com/loopdreams/flights.git

```

Then, `cd` into the repo and  just run `bb` followed by the name of the main file and the relevant arguments (detailed below).

``` sh
bb flights 'New York' 'Paris'
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
-------------------------------------------------------------------
Origin: Tegel Airport in Berlin, Germany
Destination: Getafe Airport in Madrid, Spain

Approximate Distance: 1877 km

Estimated Flight Time: Between 2 hours, 42 minutes and 3 hours, 2 minutes

[■] Carbon cost: 248 kg / 3% of annual EU avg
[■■■] Recommended annual avg: 600.0 kg
[■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■] EU Person Annual Avg: 7770 kg
-------------------------------------------------------------------
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

Similarly, if you want to search for all the cities within a country, you can use the `-c` flag (for 'cities'):

``` sh
bb flights -c 'Germany'
```

### API/Returning data

If you would like to use the functionality of this program within another pipeline, you can also request for it to return the response in either `json` or `edn` format by using the `-d` flag. For example:

``` sh
bb flights -d :json 'Warsaw' 'Dublin' | jq ".distance"
```

## Modifying and Building

The main file, `flights` depends on the `sqlite` database that is stored in the `db` folder. If you would like to turn this script into a standalone utility, you could do something along the lines of the following.

Example: storing the script in `.local/bin/flights`

1. Modify the 'db' variable in the `src/flights/db.clj` file to something like:

``` clojure
(def db "/Users/[username]/.local/bin/flights/db/global_airports_sqlite.db")
```

2. Remove the existing `flights` file and generate a new one with:

``` sh
bb build
```

3. Add the following to the top of the `flights` file:

``` sh
#!/usr/bin/env bb
```

4. Copy/move the `flights` file and the `db` folder to `.local/bin/flights`

5. Make the `flights` file executable 

``` sh
chmod +x ~/.local/bin/flights/flights
```

Now you can run the 'flights' command from anywhere. 

I haven't built any cli utilities before, so I am not very well versed in best practices regarding the procedures for automatically installing scripts/resources. The above steps are a bit cumbersome and could be easily automated. If anyone has any suggestions please reach out.

## Calculations

The script has three kinds of calculations:

- distance (based on latitude and longitude)
- flight-times (based on distance) 
- carbon cost (based on distance). 

I am not an expert on these areas and relied heavily on the types of calculations used in already-existing online tools (cited below). It is not easy to calculate the flight-time based only on distance, so this is the weakest area. 

By coincidence, I went on holiday during the month of March and flew from Dublin to Salzburg. I timed how long the journey took from when the wheels left the ground until we landed. It took 1 hour and 55 minutes to get there and 2 hours and 21 minutes to return. A pretty significant difference! This script is very simple and does not account for these kinds of differences (depending on direction of travel, etc.)

Here is what the script returns for the same flight:

``` text
-------------------------------------------------------------------
Origin: Dublin Airport in Dublin, Ireland
Destination: Salzburg Airport in Salzburg, Austria

Approximate Distance: 1490 km

Estimated Flight Time: Between 2 hours, 15 minutes and 2 hours, 30 minutes

[■] Carbon cost: 200 kg / 2% of annual EU avg
[■■■] Recommended annual avg: 600.0 kg
[■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■] EU Person Annual Avg: 7770 kg
-------------------------------------------------------------------
```

### Distance

The calculations for distance were based on reading through [this website (movable-type.co.uk)](https://www.movable-type.co.uk/scripts/latlong.html), and used the **haversine formula**.

### Flight Time 

As mentioned above, calculating flight times was very difficult, due to the high number of variables. In the end, I simply went for using the average air speed (found on google), reduced it down a bit to account for the distance covered by takeoff/landing periods of flight, and then adding 30 mins for the take off and landing. It was very approximate! But, cross checking it with some google results, it does line up in a lot of cases. The main problem is that for long distances the suggested range becomes too wide to be in any way useful (of course, for very long distances there usually aren't single/non-connecting flights anyway...)

### Carbon Cost

While doing this exercise, I discovered that there were quite a few websites dedicated to calculating the carbon cost of flights, and also offering the ability to 'offset' this cost through some kind of payment. I didn't look too much into the sites themselves, but I did borrow from this one site - [co2.myclimate.org](https://co2.myclimate.org/en/flight_calculators/new) - in how they represented the carbon cost alongside the *maximum CO2* that a single person should produce in a year to stop climate change and the *average annual* amount of CO2 produced by a single person in the EU.

Main assumptions were:

- Average fuel usage per kilometer: 12kg
- Average fuel usage for takeoff/landing: 1,100kg
- Average number of passengers on a commercial flight: 300
- Average EU person annual CO2 emissions: 7.77 tons
- Annual amount recommended for combating climate change: 0.6 tons
- Emissions Factor: 3.16

The 'emissions factor' is the amount of CO2 produced per kg of jet fuel consumed.

