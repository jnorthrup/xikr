# goal

explore incomplete knowledge and resources

# premise

after many years with the opennars community, one of the more respected members mentioned something a little bit like this:

### ....
what im experimenting with today is a major simplification of the term operators which would affect syntax in a few ways. basically i see a way to remove the need for an internal representation of the 2 remaining intersection operators and to replace them with && and --. (i already removed difference and disjunction operators by a similar de-morgan's law-based strategy).

all of the conjunction, disjunction, intersection, difference truth functions use the same "fuzzy set t-norm" calculations on frequency values -- various negation permutations of the intersection truth function for composition, and decomposition truth function for decomposition. this means they can all be reduced to recursive compounds of AND's and NOT's. ie. a difference, (A-B) can be perfectly represented as the intersection (A & --B). what makes this confusing is the distinction between intensional and extensional representation - thats why in OpenNARS there is SECT extensional and intensional, DIFF extensional and intensional, (and also SET extensional and intensional). but it seems to me that the only thing "different" between intensional and extensional terms and how they interact and/or transform into the opposite, is their contextual role (ex: whether the intersection appears in the subj or pred of inheritance). the truth functions calculating these follow alternating intersection/union (==disjunction) symmetry.

if then they can all be && and -- with context-specific rules determinig truth calculations then this results in less representation redundancy (saving memory), better interning utilization, and less derivation rules and corner cases. as well as some other nice simplifications that happen as a result, for example in temporal conjunction cases where additional redundancy can be elmiinated.

re: syntax and grammar, the goal is to enable the following. | or || or - or ~ do not actually exist internally after being parsed into the equivalent canonical AND/NOT representation unified for all 3 "levels":
'
a) intensional inheritance
(x --> (a & b))
(x --> (a | b)) == (x --> --(--a & --b))
(x --> (a - b)) == (x --> (a & --b))
(x --> (a ~ b)) == (x --> --(--a & b))

b) extensional inheritance
((a | b) --> x)
((a & b) --> x) == (--(--a & --b) --> x)
((a ~ b) --> x) == ((a & --b) --> x)
((a - b) --> x) == (--(--a & b) --> x)

c) events
(x & y)
(x | y) == --(--x & --y)
(x - y) == (x & --y)
(x~y) ==   --(--x & y)
`