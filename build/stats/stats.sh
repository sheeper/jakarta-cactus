#!/bin/tcsh
#--------------------------
# Generate stats for Cactus
#
# $1 : year
# $2 : month
# $3 : day
#--------------------------

set OUTPUT_DIR=../../target/doc/stats
set HTDOCS=/www/jakarta.apache.org/cactus

mkdir -p $OUTPUT_DIR

zcat /x2/logarchive/www/$1/$2/$3.gz | egrep "GET.*cactus.*HTTP" | webalizer -o $OUTPUT_DIR

cp -R $OUTPUT_DIR $HTDOCS
