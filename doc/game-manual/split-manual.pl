#!/usr/bin/perl
# Split rendered/manual.md into one cleaned file per major section, for upload
# to Flint (24,000-word document limit). Regenerates rendered/sections/ from
# scratch; manual.md itself is never modified.
#
#   perl split-manual.pl
#
# Cleanups applied, all aimed at making the text legible to an LLM reader:
#   - drop the ~950 <a id="_Toc..."></a> bookmark anchors pandoc emitted
#   - non-breaking spaces -> spaces, runs of spaces collapsed
#   - ^(R) / ^(TM) / ^(st) superscript notation -> real characters
#   - unescape \- \< \> \[ \] \# \* left over from the HTML conversion
#   - images -> "[Figure: alt text]" (Flint can't follow the file paths)
#   - internal #anchor links -> their link text
#   - rebuild tables: drop empty/separator rows, re-emit with a valid header
#   - rule IDs (G101, R702, ...) become #### headings so each rule is findable
#   - [TABLE] placeholders -> an explicit note that content is missing

use strict;
use warnings;
use utf8;
use File::Basename qw(dirname);
use File::Path qw(make_path);

my $dir      = dirname(__FILE__);
my $src      = "$dir/rendered/manual.md";
my $outdir   = "$dir/rendered/sections";
my $manual   = '2026-2027 *FIRST* Tech Challenge Competition Manual — BIOBUZZ, V1';

open my $in, '<:encoding(UTF-8)', $src or die "cannot read $src: $!";
my @lines = <$in>;
close $in;
chomp @lines;

# ---------------------------------------------------------------- line cleanup
my @clean;
for my $l (@lines) {
    $l =~ s{<a id="[^"]*"></a>}{}g;      # bookmark anchors
    $l =~ s{</?a\b[^>]*>}{}g;            # any stray anchor tags
    $l =~ s{\x{00a0}}{ }g;               # non-breaking spaces
    $l =~ s{\x{2011}}{-}g;               # non-breaking hyphens

    $l =~ s{\^\(®\)}{®}g;
    $l =~ s{\^\(TM\)}{™}g;
    $l =~ s{\^\((st|nd|rd|th)\)}{$1}g;

    $l =~ s{\\([-<>\[\]#*])}{$1}g;       # undo pandoc's escaping

    # images -> figure notes; alt text is the only part Flint can use
    $l =~ s{!\[([^\]]*)\]\([^)]*\)}{ my $a = $1; $a =~ s/\s+$//; $a ne '' ? "[Figure: $a]" : '[Figure]' }ge;

    # internal cross-reference links keep their text, lose the dead anchor
    $l =~ s{\[([^\]]*)\]\(#[^)]*\)}{$1}g;

    $l =~ s{\[TABLE\]}{*[A table appears here in the official manual; it did not survive conversion and is omitted.]*}g;

    $l =~ s{ {2,}}{ }g;                  # collapse runs of spaces
    $l =~ s{\s+$}{};
    $l = '' if $l =~ m{^\\\s*$};         # pandoc's lone-backslash line breaks
    $l = '' if $l =~ m{^[*\s]+$};        # "* *" separator artifacts
    push @clean, $l;
}

# --------------------------------------------------- tables, rules, blank runs
my @body;
for (my $i = 0; $i < @clean; $i++) {
    my $l = $clean[$i];

    # gather a run of table lines and rebuild it
    if ($l =~ /^\|/) {
        my @block;
        while ($i < @clean && $clean[$i] =~ /^\|/) { push @block, $clean[$i]; $i++ }
        $i--;
        push @body, rebuild_table(@block);
        next;
    }

    # G101 / R702 / I103 ... -> their own heading, with the rule text after it.
    # The source runs headline and body together on one line; the headline ends
    # at the closing "**" where it was bolded, else at its first sentence.
    if ($l =~ /^\*{0,2}([A-Z]{1,3}[0-9]{3})\s+(.*)$/) {
        my ($id, $rest) = ($1, $2);
        my $text = '';
        if ($rest =~ /^(.+?)\*\*\s*(.*)$/) {
            ($rest, $text) = ($1, $2);
        } elsif ($rest =~ /^(.{0,140}?[.:])\s+(.*)$/) {
            ($rest, $text) = ($1, $2);
        }
        # a leading asterisk marks an Evergreen rule (see Section 1.7.1)
        my $evergreen = ($rest =~ s/^\*\s*//) ? ' [Evergreen rule]' : '';
        $rest =~ s/\*+\s*$//;
        $rest =~ s/\s+$//;
        push @body, "#### $id — $rest$evergreen";
        push @body, '', $text if $text =~ /\S/;
        next;
    }

    push @body, $l;
}

# collapse 2+ blank lines
my @out;
for my $l (@body) {
    next if $l eq '' && @out && $out[-1] eq '';
    push @out, $l;
}

# ------------------------------------------------------------------ split & write
make_path($outdir);
unlink glob "$outdir/*.md";

my (@files, @titles, @counts, @gaps);
my ($n, @buf, $title) = (0);

for my $l (@out) {
    if ($l =~ /^# (.*)$/) {
        flush($n, $title, \@buf) if @buf;
        $n++;
        $title = $1;
        @buf   = ($l);
    } else {
        $title = 'Front Matter & Contents' unless defined $title;
        push @buf, $l;
    }
}
flush($n, $title, \@buf);

sub flush {
    my ($idx, $t, $buf) = @_;
    my $slug = lc $t;
    $slug =~ s/[*^()]//g;
    $slug =~ s/[^a-z0-9]+/-/g;
    $slug =~ s/^-+|-+$//g;
    my $name = sprintf '%02d-%s.md', $idx, $slug;
    push @files,  $name;
    push @titles, $t;

    my $text = join "\n", @$buf;
    $text =~ s/^\n+//; $text =~ s/\n+$//;
    my $words = () = $text =~ /\S+/g;
    push @counts, $words;
    my $gap = () = $text =~ /A table appears here/g;
    push @gaps, $gap;

    open my $fh, '>:encoding(UTF-8)', "$outdir/$name" or die $!;
    print $fh "> **$manual**\n";
    print $fh "> Part " . ($idx + 1) . ": $t\n";
    print $fh "> One major section of the competition manual. See README.md for the full part list.\n\n";
    print $fh "$text\n";
    close $fh;
}

# ------------------------------------------------------------------ table rebuild
sub rebuild_table {
    my @rows;
    for my $l (@_) {
        next if $l =~ /^\|[\s:|-]*\|?\s*$/ && $l =~ /-/;   # separator row
        my $body = $l;
        $body =~ s/^\|//; $body =~ s/\|$//;
        my @cells = map { my $c = $_; $c =~ s/^\s+|\s+$//g; $c } split /\|/, $body, -1;
        next unless grep { /\S/ } @cells;                  # wholly empty row
        push @rows, \@cells;
    }
    return () unless @rows;

    # a table that only carries figures reads better as plain lines
    my $textual = grep { my $r = $_; grep { /\S/ && !/^\[Figure/ } @$r } @rows;
    unless ($textual) {
        return ('', (map { my $r = $_; grep { /\S/ } @$r } @rows), '');
    }

    my $width = 0;
    for my $r (@rows) { $width = @$r if @$r > $width }
    my @lines = ('');
    my $first = 1;
    for my $r (@rows) {
        my @c = @$r;
        push @c, '' while @c < $width;
        push @lines, '| ' . join(' | ', @c) . ' |';
        if ($first) { push @lines, '|' . ('---|' x $width); $first = 0 }
    }
    push @lines, '';
    return @lines;
}

# ------------------------------------------------------------------ README index
my $total_words = 0;
$total_words += $_ for @counts;

open my $rd, '>:encoding(UTF-8)', "$outdir/README.md" or die $!;
print $rd <<"HEAD";
# BIOBUZZ Competition Manual — split sections

`../manual.md` split into one file per major section and cleaned up for upload
to Flint, whose document limit is 24,000 words. Every part is well under it.

Generated by `../../split-manual.pl` — edit that script and re-run it rather
than editing these files, since a re-run overwrites them.

| Part | Section | Words | Tables missing |
|---|---|---|---|
HEAD
my $total_gaps = 0;
for my $i (0 .. $#files) {
    $total_gaps += $gaps[$i];
    printf $rd "| %d | [%s](%s) | %s | %s |\n",
        $i + 1, $titles[$i], $files[$i], $counts[$i], ($gaps[$i] || '');
}
printf $rd "\nTotal: %d words across %d parts.\n", $total_words, scalar @files;
print $rd <<"TAIL";

## Known gap

$total_gaps tables were lost when the source HTML was converted to Markdown and appear
as a bracketed note in the text — including the whole Glossary. Recover them from
`../index.htm` before relying on these files as tutor reference.
TAIL
close $rd;

printf "%-52s %6d\n", $files[$_], $counts[$_] for 0 .. $#files;
printf "%-52s %6d\n", 'TOTAL', $total_words;
