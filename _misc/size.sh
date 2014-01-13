#/bin/bash

## BBStalker image preparation script
## By Raegdan for the BBStalker project

# Sizes
xhdpi=300
hdpi=225
mdpi=150

wxhdpi=96
whdpi=72
wmdpi=48

mkdir drawable-xhdpi drawable-hdpi drawable-mdpi
rm -rf drawable-xhdpi/* drawable-hdpi/* drawable-mdpi/*

echo Processing figures
for i in bb*.*; do
	echo Processing $i
	color=`convert $i -format "%[pixel: u.p{0,0}]" info:`
	
	convert $i -background "$color" -gravity center -resize $xhdpi\x$xhdpi -extent $xhdpi\x$xhdpi -flatten drawable-xhdpi/$i
	convert $i -background "$color" -gravity center -resize $hdpi\x$hdpi -extent $hdpi\x$hdpi -flatten drawable-hdpi/$i
	convert $i -background "$color" -gravity center -resize $mdpi\x$mdpi -extent $mdpi\x$mdpi -flatten drawable-mdpi/$i
done

echo Processing waves
for i in w*.*; do
	echo Processing $i
	
	convert $i -resize $wxhdpi\x$wxhdpi drawable-xhdpi/$i
	convert $i -resize $whdpi\x$whdpi drawable-hdpi/$i
	convert $i -resize $wmdpi\x$wmdpi drawable-mdpi/$i
done
