$FOLDER="$TRAVIS_BUILD_DIR/de.bund.bfr.knime.update/target/repository"
$KNIME_FILE="knime_3.7.2.win32.win32.x86_64.zip"
wget "http://download.knime.org/analytics-platform/win/$KNIME_FILE" -outfile "$KNIME_FILE"
unzip "$KNIME_FILE"
$KNIME37="https://update.knime.org/analytics-platform/3.7"
$OLD_FSK="https://dl.bintray.com/silebat/fsklab_test"
$NEW_FSK="file:$TRAVIS_BUILD_DIR/de.bund.bfr.knime.update/target/repository"

echo "INSTALL NEW FSK-LAB INTO FRESH KNIME"
knime_3.7.2/knime -nosplash -application org.eclipse.equinox.p2.director -repository "$KNIME37,$NEW_FSK" -installIU de.bund.bfr.knime.fsklab.feature.feature.group,org.knime.features.testingapplication.feature.group

echo "REMOVE NEW FSK TO TEST UPDATE FROM OLDER VERSION"
knime_3.7.2/knime -nosplash -application org.eclipse.equinox.p2.director -uninstallIU de.bund.bfr.knime.fsklab.feature.feature.group

#rm $KNIME_FILE 
rm knime_3.7.2 -recurse
