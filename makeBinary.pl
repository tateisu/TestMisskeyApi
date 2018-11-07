#!/usr/bin/perl --
use strict;
use warnings;
use Cwd;
use File::Basename qw( basename);

my $dir = basename getcwd;

my @lt = localtime;
$lt[4]+=1;
$lt[5]+=1900;
my $datetime = sprintf "%d%02d%02d_%02d%02d%02d",reverse @lt[0..5];

system qq( ./gradlew jar );
system qq(cp build/libs/testmisskeyapi-1.0-SNAPSHOT.jar $dir.jar );

my $files= join(' ',"$dir.jar",qw( tma.conf.sample README.md sample.jpg ));

system qq(zip "${dir}-jar-${datetime}.zip" $files);
