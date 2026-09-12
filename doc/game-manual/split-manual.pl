#!/usr/bin/perl
# Split the BIOBUZZ Competition Manual into one cleaned file per major section,
# for upload to Flint (24,000-word document limit).
#
#   perl split-manual.pl
#
# Source is rendered/index.htm, not rendered/manual.md. manual.md is produced by
# Update-CompetitionManual.ps1 with --to=gfm-raw_html, and GFM pipe tables cannot
# express the merged cells these tables use, so pandoc drops 25 of them -- the
# scoring tables, the award criteria and the whole Glossary -- leaving "[TABLE]".
# Converting the same HTML with grid tables enabled keeps every one of them.
#
# Needs the pandoc the updater pins; it reuses the updater's cached copy if present,
# else pandoc on PATH, else falls back to manual.md with those tables still missing.
#
# Cleanups applied, all aimed at making the text legible to an LLM reader:
#   - drop the ~950 <a id="_Toc..."></a> bookmark anchors carried over from Word
#   - drop pandoc attribute blocks ({#slug style="..."}, image width/height)
#   - non-breaking spaces -> spaces, runs of spaces collapsed
#   - ^R^ / ^TM^ / ^st^ superscript notation -> real characters
#   - unescape \- \< \> \[ \] \# \* left over from the HTML conversion
#   - images -> "[Figure: alt text]" (Flint can't follow the file paths)
#   - internal #anchor links -> their link text
#   - tables -> compact pipe tables, with merged cells repeated down their span
#   - rule IDs (G101, R702, ...) become #### headings so each rule is findable

use strict;
use warnings;
use utf8;
use Encode qw(decode encode);
use File::Basename qw(dirname);
use File::Path qw(make_path);
use File::Spec;

my $dir    = dirname(__FILE__);
my $html   = "$dir/rendered/index.htm";
my $md     = "$dir/rendered/manual.md";
my $outdir = "$dir/sections";          # outside rendered/, which the updater wipes
my $manual = '2026-2027 *FIRST* Tech Challenge Competition Manual — BIOBUZZ, V1';

my @lines = read_source();

# ------------------------------------------------------- tables, rules, blanks
# Table blocks are handled before any other cleanup: grid tables are parsed by
# column position, so rewriting their text first would shift the cell boundaries.
my @body;
for (my $i = 0; $i < @lines; $i++) {
    my $l = $lines[$i];

    if ($l =~ /^[+|]/) {
        my @block;
        while ($i < @lines && $lines[$i] =~ /^[+|]/) { push @block, $lines[$i]; $i++ }
        $i--;
        push @body, render_table(@block);
        next;
    }

    $l = tidy($l);
    next if $l =~ /^\\\s*$/;             # pandoc's lone-backslash line breaks
    $l = '' if $l =~ /^[*\s]+$/;         # "* *" separator artifacts

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

# ------------------------------------------------------------- split and write
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

# ---------------------------------------------------------------- text cleanup
sub tidy {
    my ($t) = @_;
    return '' unless defined $t;

    $t =~ s{<a id="[^"]*"></a>}{}g;      # Word bookmark anchors
    $t =~ s{</?a\b[^>]*>}{}g;            # any stray anchor tags
    $t =~ s{\x{00a0}}{ }g;               # non-breaking spaces
    $t =~ s{\x{2011}}{-}g;               # non-breaking hyphens

    $t =~ s/\[([^\]]*)\]\{\.underline\}/$1/g;    # underline spans
    $t =~ s/\s*\{[#.][^}]*\}//g;                 # heading/span attributes
    $t =~ s/\{[^}]*=[^}]*\}//g;                  # image width/height/border

    $t =~ s{\^\(?®\)?\^?}{®}g;
    $t =~ s{\^\(?(?:TM|™)\)?\^?}{™}g;
    $t =~ s{\^\((st|nd|rd|th)\)}{$1}g;   # gfm spelling
    $t =~ s{\^(st|nd|rd|th)\^}{$1}g;     # pandoc-markdown spelling

    $t =~ s{\\([-<>\[\]#*'"])}{$1}g;     # undo pandoc's escaping

    # images -> figure notes; alt text is the only part Flint can use
    $t =~ s{!\[([^\]]*)\]\([^)]*\)}{ my $a = $1; $a =~ s/\s+$//; $a ne '' ? "[Figure: $a]" : '[Figure]' }ge;

    # internal cross-reference links keep their text, lose the dead anchor
    $t =~ s{\[([^\]]*)\]\(#[^)]*\)}{$1}g;

    $t =~ s{\[TABLE\]}{*[A table appears here in the official manual; it did not survive conversion and is omitted.]*}g;

    $t =~ s/ {2,}/ /g;
    $t =~ s/^\s+|\s+$//g;
    return $t;
}

# ------------------------------------------------------------------- the source
# Prefer converting the HTML ourselves so the complex tables survive.
sub read_source {
    my $pandoc = find_pandoc();
    my $text;

    if ($pandoc) {
        # index.htm is windows-1252; pandoc only reads UTF-8, so re-encode first
        open my $h, '<:raw', $html or die "cannot read $html: $!";
        my $raw = do { local $/; <$h> };
        close $h;
        my $tmp = File::Spec->catfile(File::Spec->tmpdir, "biobuzz-utf8-$$.htm");
        open my $t, '>:raw', $tmp or die $!;
        print $t encode('UTF-8', decode('cp1252', $raw));
        close $t;

        my $out = File::Spec->catfile(File::Spec->tmpdir, "biobuzz-grid-$$.md");
        my @cmd = ($pandoc,
            '--from=html',
            # leave pipe and grid tables enabled: pandoc uses pipe where it fits
            # and grid -- the only syntax that carries merged and multi-paragraph
            # cells -- everywhere else, so no table is dropped
            '--to=markdown-raw_html-simple_tables-multiline_tables',
            '--wrap=none',
            '--markdown-headings=atx',
            "--lua-filter=$dir/../../scripts/strip-word-styles.lua",
            "--output=$out", $tmp);
        system(@cmd) == 0 or die "pandoc failed (exit " . ($? >> 8) . ")\n";

        open my $o, '<:encoding(UTF-8)', $out or die $!;
        $text = do { local $/; <$o> };
        close $o;
        unlink $tmp, $out;
        print "source: rendered/index.htm via pandoc\n";
    } else {
        warn "pandoc not found -- falling back to rendered/manual.md.\n"
           . "25 tables, including the Glossary and the scoring tables, will be missing.\n"
           . "Run scripts/Update-CompetitionManual.ps1 once to cache pandoc, then re-run.\n";
        open my $f, '<:encoding(UTF-8)', $md or die "cannot read $md: $!";
        $text = do { local $/; <$f> };
        close $f;
        print "source: rendered/manual.md\n";
    }

    my @l = split /\r?\n/, $text;
    return @l;
}

sub find_pandoc {
    return $ENV{PANDOC} if $ENV{PANDOC} && -x $ENV{PANDOC};

    # the copy Update-CompetitionManual.ps1 pins and caches
    my $version = '3.10.2';
    for my $platform (qw(windows-x86_64 linux-amd64)) {
        my $exe = $platform =~ /^windows/ ? 'pandoc.exe' : 'pandoc';
        my $p = File::Spec->catfile(File::Spec->tmpdir,
            'WinsorRobotics-tools', "pandoc-$version-$platform", "pandoc-$version", $exe);
        return $p if -x $p;
    }

    for my $p (split /[:;]/, ($ENV{PATH} // '')) {
        for my $exe ('pandoc', 'pandoc.exe') {
            my $c = File::Spec->catfile($p, $exe);
            return $c if -x $c;
        }
    }
    return undef;
}

# --------------------------------------------------------------------- tables
# Turn one table block -- grid or pipe -- into a compact pipe table.
sub render_table {
    my @block = @_;
    my $rows = (grep { /^\+/ } @block) ? parse_grid(@block) : parse_pipe(@block);

    @$rows = grep { grep { /\S/ } @$_ } @$rows;    # drop wholly empty rows
    return () unless @$rows;

    # a table that only carries figures reads better as plain lines
    my $textual = grep { my $r = $_; grep { /\S/ && !/^\[Figure/ } @$r } @$rows;
    unless ($textual) {
        return ('', (map { my $r = $_; grep { /\S/ } @$r } @$rows), '');
    }

    my $width = 0;
    for my $r (@$rows) { $width = @$r if @$r > $width }

    my @lines = ('');
    my $first = 1;
    for my $r (@$rows) {
        my @c = @$r;
        push @c, '' while @c < $width;
        s/\|/\\|/g for @c;
        push @lines, '| ' . join(' | ', @c) . ' |';
        if ($first) { push @lines, '|' . ('---|' x $width); $first = 0 }
    }
    push @lines, '';
    return @lines;
}

# A pipe table: one row per line, minus the separator.
sub parse_pipe {
    my @rows;
    for my $l (@_) {
        next if $l =~ /^\|[\s:|-]*\|?\s*$/ && $l =~ /-/;
        my $b = $l;
        $b =~ s/^\|//;
        $b =~ s/\|\s*$//;
        push @rows, [ map { tidy($_) } split /\|/, $b, -1 ];
    }
    return \@rows;
}

# A grid table. Cells are located by column position, because a merged cell
# simply omits the "|" at the boundary it spans. Rules that start mid-line
# (they begin with "|") close a sub-row inside a row-spanning group: whatever
# columns that rule does not cover are still spanned, so their value is carried
# down and repeated -- which is what makes the scoring tables readable as rows.
sub parse_grid {
    my @block = @_;
    my ($top) = grep { /^\+/ } @block;
    my @bounds;
    while ($top =~ /\+/g) { push @bounds, pos($top) - 1 }
    return parse_pipe(@block) if @bounds < 2;

    my (@rows, @pending, @carry);

    for my $l (@block) {
        if ($l =~ /^\+/) {                      # full-width rule: row group ends
            push @rows, close_row(\@pending, \@carry, \@bounds) if @pending;
            @pending = ();
            @carry   = ();
            next;
        }
        if ($l =~ /\+[-=]/) {                   # interior rule: sub-row ends
            push @rows, close_row(\@pending, \@carry, \@bounds) if @pending;
            @pending = ();
            for my $i (0 .. $#bounds - 1) {
                my $seg = substr($l, $bounds[$i], $bounds[$i + 1] - $bounds[$i] + 1);
                $carry[$i] = undef if $seg =~ /[-=]/;   # this column is not spanned
            }
            next;
        }
        push @pending, $l;
    }
    push @rows, close_row(\@pending, \@carry, \@bounds) if @pending;
    return \@rows;
}

# One sub-row: its physical lines, the values spanned down from above, and the
# column boundaries. Returns the finished list of cell strings.
sub close_row {
    my ($lines, $carry, $bounds) = @_;
    my @cells;

    for my $l (@$lines) {
        my @pipes;
        while ($l =~ /\|/g) { push @pipes, pos($l) - 1 }
        next if @pipes < 2;
        for my $p (0 .. $#pipes - 1) {
            my ($from, $to) = ($pipes[$p], $pipes[$p + 1]);
            my $text = substr($l, $from + 1, $to - $from - 1);
            next unless $text =~ /\S/;
            $text =~ s/\\\s*$//;                # hard line break inside a cell
            $text = tidy($text);
            next if $text eq '';
            my $col = column_of($from, $bounds);
            $cells[$col] = defined $cells[$col] && $cells[$col] ne ''
                ? "$cells[$col] $text" : $text;
        }
    }

    for my $i (0 .. $#$bounds - 1) {
        if (defined $cells[$i] && $cells[$i] ne '') {
            $carry->[$i] = $cells[$i];                   # may span rows below
        } elsif (defined $carry->[$i]) {
            $cells[$i] = $carry->[$i];                   # repeat the spanned value
        }
    }
    $_ = defined $_ ? $_ : '' for @cells;
    return \@cells;
}

# Which column does a "|" at this position open? Merged cells and rows whose
# rule lines sit at slightly different offsets mean it may not land exactly on
# a boundary, so take the nearest boundary at or before it.
sub column_of {
    my ($pos, $bounds) = @_;
    my $col = 0;
    for my $i (0 .. $#$bounds - 1) {
        $col = $i if $bounds->[$i] <= $pos;
    }
    return $col;
}

# ------------------------------------------------------------------ README index
my $total_words = 0;
$total_words += $_ for @counts;

open my $rd, '>:encoding(UTF-8)', "$outdir/README.md" or die $!;
print $rd <<"HEAD";
# BIOBUZZ Competition Manual — split sections

The competition manual split into one file per major section and cleaned up for
upload to Flint, whose document limit is 24,000 words. Every part is well under it.

Generated by `../split-manual.pl` from `../rendered/index.htm` — edit that script
and re-run it rather than editing these files, since a re-run overwrites them.
Re-run it after `scripts/Update-CompetitionManual.ps1` picks up a new revision.

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
if ($total_gaps) {
    print $rd <<"TAIL";

## Known gap

$total_gaps tables are still missing — the split ran against `../rendered/manual.md`
rather than the HTML, so pandoc's `[TABLE]` placeholders came through. Cache pandoc
by running `scripts/Update-CompetitionManual.ps1` once, then re-run the splitter.
TAIL
}
close $rd;

printf "%-52s %6d%s\n", $files[$_], $counts[$_], ($gaps[$_] ? "  ($gaps[$_] tables missing)" : '')
    for 0 .. $#files;
printf "%-52s %6d\n", 'TOTAL', $total_words;
