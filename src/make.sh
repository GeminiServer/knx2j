
rm ../release/knx2j.json
rm ../release/knx2j.jar

jar cfm ../release/knx2j.jar manifest.mf knx2j.class G3m1n1S3rv3r/*.class com/eclipsesource/json/*.class
cp knx2j.json ../release/knx2j.json
