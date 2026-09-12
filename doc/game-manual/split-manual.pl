#!/usr/bin/perl
# Split the BIOBUZZ Competition Manual into one cleaned file per major section,
# for upload to Flint (24,000-word document limit).
#
#   perl split-manual.pl
#
# Source is rendered/index.htm, not rendered/manual.md. manual.md is produced by
# Update-CompetitionManual.ps1 with --to=gfm-raw_html, and GFM pipe tables cannot
# express the merged cells these tables use, so pandoc drops 25 of them -- the
# scoring tables, the award criteria and the entire Glossary -- leaving "[TABLE]".
# Converting the same HTML to grid tables keeps all of them, and this script then
# folds them back down to pipe tables.
#
# Needs the same pandoc the updater pins; it reuses the updater's cached copy if
# present, else pandoc on PATH, else falls back to manual.md with the tables missing.
#
# Cleanups applied, all aimed at making the text legible to an LLM reader:
#   - drop the ~950 <a id="_Toc..."></a> bookmark anchors carried over from Word
#   - drop pandoc attribute blocks ({#slug style="..."}, image width/height)
#   - non-breaking spaces -> spaces, runs of spaces collapsed
#   - ^R^ / ^TM^ / ^st^ superscript notation -> real characters
#   - unescape \- \< \> \[ \] \# \* left over from the HTML conversion
#   - images -> "[Figure: alt text]" (Flint can't follow the file paths)
#   - internal #anchor links -> their link text
#   - grid tables -> pipe tables; empty and figure-only tables flattened
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

# ---------------------------------------------------------------- line cleanup
my @clean;
for my $l (@lines) {
    $l =~ s{<a id="[^"]*"></a>}{}g;      # Word bookmark anchors
    $l =~ s{</?a\b[^>]*>}{}g;            # any stray anchor tags
    $l =~ s{\x{00a0}}{ }g;               # non-breaking spaces
    $l =~ s{\x{2011}}{-}g;               # non-breaking hyphens

    $l =~ s{\[([^\]]*)\]\{\.underline\}}{$1}g;   # underline spans
    $l =~ s{\s*\{[#.][^}]*\}}{}g;                # heading/span attributes
    $l =~ s{\{[^}]*=[^}]*\}}{}g;                 # image width/height/border

    $l =~ s{\^\(?®\)?\^?}{®}g;
    $l =~ s{\^\(?(?:TM|™)\)?\^?}{™}g;
    $l =~ s{\^\((st|nd|rd|th)\)}{$1}g;   # gfm spelling
    $l =~ s{\^(st|nd|rd|th)\^}{$1}g;     # pandoc-markdown spelling

    $l =~ s{\\([-<>\[\]#*'"])}{$1}g;     # undo pandoc's escaping

    # images -> figure notes; alt text is the only part Flint can use
    $l =~ s{!\[([^\]]*)\]\([^)]*\)}{ my $a = $1; $a =~ s/\s+$//; $a ne '' ? "[Figure: $a]" : '[Figure]' }ge;

    # internal cross-reference links keep their text, lose the dead anchor
    $l =~ s{\[([^\]]*)\]\(#[^)]*\)}{$1}g;

    $l =~ s{\[TABLE\]}{*[A table appears here in the official manual; it did not survive conversion and is omitted.]*}g;

    $l =~ s{\s+$}{};
    $l = '' if $l =~ m{^\\\s*$};         # pandoc's lone-backslash line breaks
    $l = '' if $l =~ m{^[*\s]+$};        # "* *" separator artifacts
    push @clean, $l;
}

# --------------------------------------------------- tables, rules, blank runs
my @body;
for (my $i = 0; $i < @clean; $i++) {
    my $l = $clean[$i];

    # a table block: grid (+---+ rules) or pipe. Collect and re-emit compactly.
    if ($l =~ /^[+|]/) {
        my @block;
        while ($i < @clean && $clean[$i] =~ /^[+|]/) { push @block, $clean[$i]; $i++ }
        $i--;
        push @body, rebuild_table(@block);
        next;
    }

    # squeeze runs of spaces only outside tables, where they carry no meaning
    $l =~ s{ {2,}}{ }g;

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
            # grid tables only: the one markdown table syntax that can carry
            # merged cells and multi-paragraph cells without data loss
            '--to=markdown-raw_html-simple_tables-multiline_tables-pipe_tables',
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

# ------------------------------------------------------------------ table rebuild
# Accepts a grid table (+---+ rules) or a pipe table and returns a compact pipe
# table. Grid cells may span several lines; those are joined back into one.
sub rebuild_table {
    my @block = @_;
    my (@rows, @pending);

    for my $l (@block) {
        if ($l =~ /^\+/) {                       # rule line ends the current row
            push @rows, join_row(@pending) if @pending;
            @pending = ();
            next;
        }
        next if $l =~ /^\|[\s:|-]*\|?\s*$/ && $l =~ /-/;   # pipe-table separator
        push @pending, $l;
    }
    push @rows, join_row(@pending) if @pending;

    @rows = grep { grep { /\S/ } @$_ } @rows;    # drop wholly empty rows
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

# one row, possibly spread over several physical lines, -> list of cell strings
sub join_row {
    my @cells;
    for my $l (@_) {
        my $b = $l;
        $b =~ s/^\|//;
        $b =~ s/\|\s*$//;
        my @c = split /\|/, $b, -1;
        for my $i (0 .. $#c) {
            my $t = $c[$i];
            $t =~ s/\\\s*$//;                    # hard line break inside a cell
            $t =~ s/^\s+|\s+$//g;
            $t =~ s/ {2,}/ /g;
            next if $t eq '';
            $cells[$i] = (defined $cells[$i] && $cells[$i] ne '') ? "$cells[$i] $t" : $t;
        }
    }
    $_ = defined $_ ? $_ : '' for @cells;
    return \@cells;
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
