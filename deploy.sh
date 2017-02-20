#!/usr/bin/env bash
set -x

rm -rf site
mkdocs build
cd api-doc-generator
boot codox target
cd ..
rm -rf site/api
mv api-doc-generator/target/doc site/api

WD=`pwd`
TMP=`mktemp -d`

cd $TMP
git clone git@github.com:arachne-framework/arachne-docs.git
cd arachne-docs
git checkout gh-pages
rm -rf *
cp -r $WD/site/ .

git add .
git add . -u
git commit -m "generating docs via deploy.sh"
git push

echo "TEMPDIR IS" $TMP

