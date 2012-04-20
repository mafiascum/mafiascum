# bash script to create a custom pgn4web package

pgn4webBaseFiles="README.txt pgn4web.js pgn4web-help.html license-gpl-2.0.txt pawn.ico pawns.png index.html"

case "$1" in

  "board-minimal" ) pgn4webPackageFileList="$pgn4webBaseFiles board.html chess-informant-NAG-symbols.js fide-lookup.js pgn-decoder.js uscf/index.html uscf/README.txt uscf/20/* fonts/index.html fonts/README.txt fonts/LiberationSans* fonts/pgn4web-font-LiberationSans.css fonts/ChessSansPiratf* fonts/pgn4web-font-ChessSansPiratf.css fonts/ChessInformantReader* fonts/pgn4web-font-ChessInformantReader.css";;

  "live-minimal" ) pgn4webPackageFileList="$pgn4webBaseFiles dynamic-frame.html live-compact.* live-fullscreen.html live-mosaic* live-results.html live*.pgn demoLiveGames.pgn alpha/index.html alpha/README.txt alpha/24/* alpha/26/* alpha/96/* merida/index.html merida/README.txt merida/96/* uscf/index.html uscf/README.txt uscf/96/* fonts/index.html fonts/README.txt fonts/LiberationSans* fonts/pgn4web-font-LiberationSans.css fonts/ChessSansPiratf* fonts/pgn4web-font-ChessSansPiratf.css fonts/ChessInformantReader* fonts/pgn4web-font-ChessInformantReader.css live/index.html live/live-grab* live/live-simulation.sh";;

  * ) echo "bash `basename $0` [board-minimal | live-minimal]"; exit 0 ;;

esac

pgn4webVer=$(grep "var pgn4web_version = " pgn4web.js | awk -F "\'" '{print$2}')

pgn4webPackageFilename="pgn4web-$pgn4webVer-$1.zip"

if [ -e "$pgn4webPackageFilename" ]; then
  echo "Error: pgn4web package already exists ($pgn4webPackageFilename)"
  exit 1
fi

zip -9r "$pgn4webPackageFilename" $pgn4webPackageFileList -x *.svn/*

echo
if [ $? -eq 0 ];
then
  echo "Package $pgn4webPackageFilename ready"
else
 echo "Error creating package $pgn4webPackageFilename"
fi

