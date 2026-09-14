#!/usr/bin/perl
# Make Word copies of the reference docs, because Flint accepts .docx uploads
# and not Markdown. Each <dir>/*.md becomes <dir>/word/<name>.docx.
#
#   perl make-word-copies.pl                 # rev-duo and limelight3a
#   perl make-word-copies.pl some/other/dir  # any directory of .md files
#
# Flint also caps a document at 24,000 words; anything over is reported so it can
# be split first. The competition manual is the one doc that needs splitting, and
# game-manual/split-manual.pl already handles it (and writes its own .docx).
#
# Needs the pandoc that Update-CompetitionManual.ps1 pins and caches, or pandoc
# on PATH.

use strict;
use warnings;
use utf8;
use File::Basename qw(dirname basename);
use File::Path qw(make_path);
use File::Spec;

my $LIMIT = 24_000;
my $dir   = dirname(__FILE__);
my @dirs  = @ARGV ? @ARGV : ("$dir/rev-duo", "$dir/limelight3a");

my $pandoc = find_pandoc()
    or die "pandoc not found. Run scripts/Update-CompetitionManual.ps1 once to cache it,\n"
         . "or put pandoc on PATH, then re-run.\n";

my $over = 0;
for my $d (@dirs) {
    my @md = sort glob "$d/*.md";
    unless (@md) {
        warn "no .md files in $d -- skipping\n";
        next;
    }

    my $out = "$d/word";
    make_path($out);
    unlink glob "$out/*.docx";

    print basename($d) . "\n";
    for my $src (@md) {
        my $name = basename($src, '.md');
        my ($words, $title) = scan($src);
        $title = $name unless defined $title;

        my @cmd = ($pandoc,
            '--from=gfm',
            '--to=docx',
            "--metadata=title:$title",
            "--output=$out/$name.docx", $src);
        system(@cmd) == 0 or die "pandoc failed on $src (exit " . ($? >> 8) . ")\n";

        my $flag = '';
        if ($words > $LIMIT) { $flag = "  OVER Flint's $LIMIT-word limit -- split this one"; $over++ }
        printf "  %-40s %6d words%s\n", "$name.docx", $words, $flag;
    }

    open my $rd, '>:encoding(UTF-8)', "$out/README.md" or die $!;
    print $rd <<"NOTE";
# Word copies

The `.md` files one directory up, converted to Word for upload to Flint, which
takes `.docx` and not Markdown. Generated — re-run `doc/make-word-copies.pl`
after editing the Markdown rather than editing these.
NOTE
    close $rd;
}

print "\n$over document(s) over Flint's $LIMIT-word limit.\n" if $over;

# words in the document, and its first heading for the Word title
sub scan {
    my ($path) = @_;
    open my $f, '<:encoding(UTF-8)', $path or die "cannot read $path: $!";
    my ($words, $title) = (0, undef);
    while (my $l = <$f>) {
        $title = $1 if !defined $title && $l =~ /^#\s+(.*\S)/;
        $words += () = $l =~ /\S+/g;
    }
    close $f;
    return ($words, $title);
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
