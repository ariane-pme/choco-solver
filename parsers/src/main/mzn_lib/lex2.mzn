%-----------------------------------------------------------------------------%
% Require adjacent rows and adjacent columns in the array 'x' to be
% lexicographically ordered.  Adjacent rows and adjacent columns may be equal.
%-----------------------------------------------------------------------------%

include "lex_lesseq.mzn";

predicate lex2(array[int, int] of var int: x) =
    lex2Choco(x,false);

