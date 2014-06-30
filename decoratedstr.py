# -*- encoding: utf-8 -*-

from __future__ import print_function, unicode_literals
import six

__all__ = ['remove_decoration', 'decorated_match']

# for range \u00c0 \u0179
char_to_alternatives_lower = {
    'a': 'àáâãäåāăą',
    'c': 'çćĉċč',
    'd': 'ďđ',
    'e': 'èéêëēĕėęě',
    'g': 'ĝğġģ',
    'h': 'ĥħ',
    'i': 'ìíîïĩīĭįı',
    'j': 'ĵ',
    'k': 'ķ',
    'l': 'ĺļľŀł',
    'n': 'ñńņňŉŋ',
    'o': 'òóôöøōŏő',
    'r': 'ŕŗř',
    's': 'śŝşš',
    't': 'ţťŧ',
    'u': 'ùúûüũūŭůűų',
    'w': 'ŵ',
    'y': 'ýÿŷ',
    'z': 'źżž',
}

# This chars lower() function doesn't work
char_to_alternatives_upper = {
    'I': 'İ',
}
char_to_alternatives = {} # idem, but with upper case too
for char, alternatives in six.iteritems(char_to_alternatives_lower):
    char_to_alternatives[char] = alternatives
    char_to_alternatives[char.upper()] = alternatives.upper()
for char, alternatives in six.iteritems(char_to_alternatives_upper):
    char_to_alternatives[char] = alternatives

alternative_to_char = {} # reverse
for char, alternatives in six.iteritems(char_to_alternatives_lower):
    for alternative in alternatives:
        alternative_to_char[alternative] = char
        alternative_to_char[alternative.upper()] = char.upper()
for char, alternatives in six.iteritems(char_to_alternatives_upper):
    for alternative in alternatives:
        alternative_to_char[alternative] = char

# ligatures (only two chars supported)
ligatures_expansions_lower = {
    'æ': 'ae',
    # 'ĳ': 'ij', buggy: see http://en.wikipedia.org/wiki/Typographic_ligature
    'œ': 'oe',
}

ligatures_expansions = {} # idem, but with upper case too
for ligature, expansion in six.iteritems(ligatures_expansions_lower):
    ligatures_expansions[ligature] = expansion
    ligatures_expansions[ligature.upper()] = expansion[0].upper()+expansion[1:]

ligatures_contractions = {} # reverse
for ligature, expansion in six.iteritems(ligatures_expansions_lower):
    ligatures_contractions[expansion] = ligature
    ligatures_contractions[expansion[0].upper()+expansion[1:]] = ligature.upper()

def remove_decoration(txt):
    result = ''
    for l in txt:
        l = alternative_to_char.get(l, l)
        l = ligatures_expansions.get(l, l)
        result += l
    return result

def decorated_match_single_char(c, casesensitive=False):
    assert type(c) == type('')
    if not casesensitive:
        c = c.lower()
    result = c + char_to_alternatives.get(c, '')
    if not casesensitive:
        u = result.upper()
        if result != u:
            result += u
    if len(result) > 1:
        return '['+result+']'
    else:
        return result

def decorated_match(txt, casesensitive=False):
    assert type(txt) == type('')
    result = ''
    txt = remove_decoration(txt)
    if not casesensitive:
        txt = txt.lower()
    i = 0
    while i < len(txt):
        c1 = txt[i] # current character
        c12 = txt[i:i+2] # both current and next characters. Contains a single char on last iteration so that it never matches, that is OK
        ligature = ligatures_contractions.get(c12, None)
        if ligature:
            result += '('+ligature
            if not casesensitive:
                result += '|'+ligature.upper()
            result += '|'+decorated_match_single_char(c12[0], casesensitive) \
                          +decorated_match_single_char(c12[1], casesensitive) \
                   +')'
            i += 1 # skip next character, we allready did both
        else:
            result += decorated_match_single_char(c1, casesensitive)
        i += 1
    return result


if __name__ == '__main__':
    import sys
    from optparse import OptionParser
    parser = OptionParser(usage='%prog [options] string')
    parser.add_option('--charset', help="set charset. default=%default", action='store', dest='charset', default='utf-8')
    parser.add_option('-r', '--regexp', help="generate regular expression.", action='store_true', dest='regexp')
    parser.add_option('-i', help="used with -r, make regexp case insensitive.", action='store_false', dest='casesensitive', default=True)
    (options, args) = parser.parse_args()

    if not args:
        print('Missing required parameter. Try "Œuf"', file=sys.stderr)
        sys.exit(1)
    if six.PY3:
        input = ' '.join(args)
    else:
        input = unicode(b' '.join(args), options.charset)
    #print("input:", input)                            # Œuf
    #print("undecorated:", remove_decoration(input))   # Oeuf
    #print("regex:", decorated_match(input))           # (œ|Œ|[oòóôöøōŏőOÒÓÔÖØŌŎŐ][eèéêëēĕėęěEÈÉÊËĒĔĖĘĚ])[uùúûüũūŭůűųUÙÚÛÜŨŪŬŮŰŲ][fF]
    if options.regexp:
        if six.PY3:
            print(decorated_match(input, options.casesensitive))
        else:
            print(decorated_match(input, options.casesensitive).encode(options.charset))
    else:
        if six.PY3:
            print(remove_decoration(input))
        else:
            print(remove_decoration(input).encode(options.charset))
